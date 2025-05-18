package no.vebb.f1.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.UserScore;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Guesser;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Component
public class GraphCache {

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@Autowired
	private Cutoff cutoff;

	private volatile Graph graph;

	private volatile Table leaderboard;

	private static final Logger logger = LoggerFactory.getLogger(GraphCache.class);

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

	public Graph getGraph() {
		if (graph == null) {
			setGraph();
		}
		return graph;
	}

	public Table getleaderboard() {
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

	private Graph getGraphHelper() {
		Graph graph = new Graph();
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		List<UUID> guessers = db.getSeasonGuesserIds(year);
		List<String> guessersNames = db.getSeasonGuessers(year);
		graph.guessers = guessersNames;
		List<RaceId> raceIds = getSeasonRaceIds(year);
		List<List<Integer>> scores = new ArrayList<>();
		for (UUID id : guessers) {
			scores.add(
				raceIds.stream()
					.map(raceId -> new UserScore(id, year, raceId, db).getScore().value)
					.toList());
		}
		graph.scores = scores;
		return graph;
	}

	private List<RaceId> getSeasonRaceIds(Year year) {
		List<RaceId> raceIds = new ArrayList<>();
		raceIds.add(null);
		db.getRaceIdsFinished(year).forEach(id -> raceIds.add(id));
		return raceIds;
	}

	private Table getLeaderboardHelper() {
		List<String> header = Arrays.asList("Plass", "Navn", "Poeng");
		List<List<String>> body = new ArrayList<>();
		if (cutoff.isAbleToGuessCurrentYear()) {
			return new Table("Sesongen starter snart", new ArrayList<>(), new ArrayList<>());
		}
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			List<Guesser> leaderboard = db.getAllUserIds().stream()
				.map(id -> {
					UserScore userScore = new UserScore(id, year, db);
					User user = userService.loadUser(id).get();
					return new Guesser(user.username, userScore.getScore(), id);
				})
				.filter(guesser -> guesser.points.value > 0)
				.sorted(Collections.reverseOrder())
				.toList();

			for (int i = 0; i < leaderboard.size(); i++) {
				Guesser guesser = leaderboard.get(i);
				String ranking = String.valueOf(i+1);
				if (i > 0 && guesser.points.equals(leaderboard.get(i-1).points)) {
					ranking = body.get(i-1).get(0);
				}
				body.add(Arrays.asList(
					ranking,
					guesser.username,
					String.valueOf(guesser.points),
					guesser.id.toString()
				));
			}
			return new Table("Rangering", header, body);
		} catch (InvalidYearException e) {
			return new Table("Det vil snart være mulig å tippe", new ArrayList<>(), new ArrayList<>());
		}
	}
}
