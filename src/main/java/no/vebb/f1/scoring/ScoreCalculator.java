package no.vebb.f1.scoring;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.PublicUser;
import no.vebb.f1.user.User;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Placement;
import no.vebb.f1.util.collection.userTables.Summary;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Position;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;

import java.util.function.Function;

@Component
public class ScoreCalculator {
    private final Database db;
    private final Cutoff cutoff;

    private static final Logger logger = LoggerFactory.getLogger(ScoreCalculator.class);

    public ScoreCalculator(Database db, Cutoff cutoff) {
        this.db = db;
        this.cutoff = cutoff;
    }

    @Transactional
    @Scheduled(initialDelay = TimeUtil.SECOND * 10)
    public void calculateScores() {
        logger.info("Calculating scores");
        try {
            calculate();
        } catch (InvalidYearException e) {
            logger.warn("Failed to calculate scores due to invalid year: {}", e.getMessage());
        }
        logger.info("Finished calculating scores");
    }

    private void calculate() {
        if (cutoff.isAbleToGuessCurrentYear()) {
            return;
        }
        Year year = new Year(TimeUtil.getCurrentYear(), db);
        List<User> guessers = db.getAllUsers();
        List<RaceId> raceIds = getSeasonRaceIds(year);
        for (RaceId raceId : raceIds) {
            Map<UUID, Summary> rankedGuessers = new HashMap<>();
            List<UserScore> userScores = guessers.stream()
                    .map(guesser -> new UserScore(new PublicUser(guesser), year, raceId, db))
                    .toList();
            Map<UUID, Placement<Points>> driversPoints = getPlacementMap(userScores, UserScore::getDriversScore);
            Map<UUID, Placement<Points>> constructorsPoints = getPlacementMap(userScores, UserScore::getConstructorsScore);
            Map<UUID, Placement<Points>> flagPoints = getPlacementMap(userScores, UserScore::getFlagScore);
            Map<UUID, Placement<Points>> winnerPoints = getPlacementMap(userScores, UserScore::getWinnerScore);
            Map<UUID, Placement<Points>> tenthPoints = getPlacementMap(userScores, UserScore::getTenthScore);
            Map<UUID, Placement<Points>> totalPoints = getPlacementMap(userScores, UserScore::getScore);

            for (User guesser : guessers) {
                UUID id = guesser.id();
                Summary summary = new Summary(
                        driversPoints.get(id),
                        constructorsPoints.get(id),
                        flagPoints.get(id),
                        winnerPoints.get(id),
                        tenthPoints.get(id),
                        totalPoints.get(id)
                );
                rankedGuessers.put(id, summary);
            }
            for (Entry<UUID, Summary> entry : rankedGuessers.entrySet()) {
                UUID id = entry.getKey();
                Summary summary = entry.getValue();
                db.addUserScore(id, summary, raceId, year);
            }
        }
    }

    private Map<UUID, Placement<Points>> getPlacementMap(List<UserScore> userScores, Function<UserScore, Points> getScore) {
        List<ScoredUser> scoredUsers = userScores.stream()
                .map(userScore -> new ScoredUser(userScore.user.id, getScore.apply(userScore)))
                .sorted(Collections.reverseOrder())
                .toList();
        Map<UUID, Placement<Points>> placementMap = new HashMap<>();
        Placement<Points> previousPlacement = null;
        for (int i = 0; i < scoredUsers.size(); i++) {
            ScoredUser scoredUser = scoredUsers.get(i);
            Placement<Points> placement;
            if (previousPlacement != null && previousPlacement.value().equals(scoredUser.score())) {
                placement = new Placement<>(previousPlacement.pos(), scoredUser.score());
            } else {
                placement = new Placement<>(new Position( i+1), scoredUser.score());
            }
            placementMap.put(scoredUser.id, placement);
            previousPlacement = placement;
        }
        return placementMap;
    }

    private List<RaceId> getSeasonRaceIds(Year year) {
        List<RaceId> raceIds = new ArrayList<>();
        raceIds.add(null);
        raceIds.addAll(db.getRaceIdsFinished(year));
        return raceIds;
    }

    private record ScoredUser(UUID id, Points score) implements Comparable<ScoredUser> {

        @Override
        public int compareTo(ScoredUser o) {
            return score.compareTo(o.score);
        }
    }

}
