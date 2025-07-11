package no.vebb.f1.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.UserScore;
import no.vebb.f1.user.PublicUser;
import no.vebb.f1.user.User;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Guesser;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Component
public class GraphCache {

	private final Database db;
	private final Cutoff cutoff;

	private volatile List<GuesserPointsSeason> graph;
	private volatile List<RankedGuesser> leaderboard;
	private static final Logger logger = LoggerFactory.getLogger(GraphCache.class);

	public GraphCache(Database db, Cutoff cutoff) {
		this.db = db;
		this.cutoff = cutoff;
	}

	@Scheduled(fixedDelay = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.SECOND * 10)
	public void refresh() {
		logger.info("Refreshing graph and leaderboard");
		try {
			setGraph();
			setLeaderboard();
		} catch (InvalidYearException e) {
			logger.warn("Failed to set graph due to invalid year: {}", e.getMessage());
		}
	}

	public List<GuesserPointsSeason> getGraph() {
		if (graph == null) {
			setGraph();
		}
		return graph;
	}

	public List<RankedGuesser> getleaderboard() {
		if (leaderboard == null) {
			setLeaderboard();
		}
		return leaderboard;
	}

	private synchronized void setGraph() {
		this.graph = getGraphHelper();
		logger.info("Graph set");
	}

	private synchronized void setLeaderboard() {
		this.leaderboard = getLeaderboardHelper();
		logger.info("Leaderboard set");
	}

	private List<GuesserPointsSeason> getGraphHelper() {
		if (cutoff.isAbleToGuessCurrentYear()) {
			return null;
		}
		List<GuesserPointsSeason> graph = new ArrayList<>();
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		List<User> guessers = db.getSeasonGuessers(year);
		List<RaceId> raceIds = getSeasonRaceIds(year);
		for (User guesser : guessers) {
			graph.add(new GuesserPointsSeason(
							guesser.username(),
				raceIds.stream()
					.map(raceId -> new UserScore(new PublicUser(guesser), year, raceId, db).getScore().value)
					.toList())
				);
		}
		return graph;
	}

	private List<RaceId> getSeasonRaceIds(Year year) {
		List<RaceId> raceIds = new ArrayList<>();
		raceIds.add(null);
        raceIds.addAll(db.getRaceIdsFinished(year));
		return raceIds;
	}

	private List<RankedGuesser> getLeaderboardHelper() {
		List<RankedGuesser> result = new ArrayList<>();
		if (cutoff.isAbleToGuessCurrentYear()) {
			return null;
		}
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			List<Guesser> leaderboard = db.getAllUsers().stream()
				.map(user -> {
					UserScore userScore = new UserScore(new PublicUser(user), year, db);
					return new Guesser(user.username(), userScore.getScore(), user.id());
				})
				.filter(guesser -> guesser.points().value > 0)
				.sorted(Collections.reverseOrder())
				.toList();

			for (int i = 0; i < leaderboard.size(); i++) {
				Guesser guesser = leaderboard.get(i);
				int rank = i+1;
				if (i > 0 && guesser.points().equals(leaderboard.get(i - 1).points())) {
					rank = result.get(i - 1).rank();
				}
				result.add(new RankedGuesser(guesser, rank));
			}
			return result;
		} catch (InvalidYearException e) {
			return null;
		}
	}
}
