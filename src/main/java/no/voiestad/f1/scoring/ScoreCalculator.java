package no.voiestad.f1.scoring;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.placement.collection.PlacementObj;
import no.voiestad.f1.placement.PlacementService;
import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.results.ResultService;
import no.voiestad.f1.user.PublicUserDto;
import no.voiestad.f1.user.UserEntity;
import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.user.UserService;
import no.voiestad.f1.util.TimeUtil;
import no.voiestad.f1.collection.Placement;
import no.voiestad.f1.scoring.userTables.Summary;
import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.year.YearService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreCalculator {
    private final CutoffService cutoffService;
    private final YearService yearService;

    private static final Logger logger = LoggerFactory.getLogger(ScoreCalculator.class);
    private final RaceService raceService;
    private final PlacementService placementService;
    private final GuessService guessService;
    private final ScoreService scoreService;
    private final ResultService resultService;
    private final UserService userService;

    public ScoreCalculator(CutoffService cutoffService, YearService yearService, RaceService raceService, PlacementService placementService, GuessService guessService, ScoreService scoreService, ResultService resultService, UserService userService) {
        this.cutoffService = cutoffService;
        this.yearService = yearService;
        this.raceService = raceService;
        this.placementService = placementService;
        this.guessService = guessService;
        this.scoreService = scoreService;
        this.resultService = resultService;
        this.userService = userService;
    }

    @Transactional
    @Scheduled(initialDelay = TimeUtil.MINUTE)
    public void calculateScores() {
        logger.info("Calculating scores");
        if (calculate()) {
            logger.info("Finished calculating scores");
        } else {
            logger.warn("Did not calculate scores");
        }
    }

    private boolean calculate() {
        if (cutoffService.getCurrentYearIfAbleToGuess().isPresent()) {
            return false;
        }
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            return false;
        }
        Year year = optYear.get();
        List<UserEntity> guessers = userService.getAllUsers();
        List<RaceId> raceIds = getSeasonRaceIds(year);
        List<PlacementObj> placementObjs = new ArrayList<>();
        for (RaceId raceId : raceIds) {
            Map<UUID, Summary> rankedGuessers = new HashMap<>();
            List<UserScore> userScores = guessers.stream()
                    .map(guesser -> new UserScore(PublicUserDto.fromEntity(guesser), year, raceId, raceService, guessService, scoreService, resultService))
                    .toList();
            Map<UUID, Placement<UserPoints>> driversPoints = getPlacementMap(userScores, UserScore::getDriversScore);
            Map<UUID, Placement<UserPoints>> constructorsPoints = getPlacementMap(userScores, UserScore::getConstructorsScore);
            Map<UUID, Placement<UserPoints>> flagPoints = getPlacementMap(userScores, UserScore::getFlagScore);
            Map<UUID, Placement<UserPoints>> winnerPoints = getPlacementMap(userScores, UserScore::getWinnerScore);
            Map<UUID, Placement<UserPoints>> tenthPoints = getPlacementMap(userScores, UserScore::getTenthScore);
            Map<UUID, Placement<UserPoints>> totalPoints = getPlacementMap(userScores, UserScore::getScore);

            for (UserEntity guesser : guessers) {
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
                placementObjs.add(new PlacementObj(id, summary, raceId, year));
            }
        }
        placementService.addUserScores(placementObjs);
        return true;
    }

    private Map<UUID, Placement<UserPoints>> getPlacementMap(List<UserScore> userScores, Function<UserScore, UserPoints> getScore) {
        List<ScoredUser> scoredUsers = userScores.stream()
                .map(userScore -> new ScoredUser(userScore.user.id(), getScore.apply(userScore)))
                .sorted(Collections.reverseOrder())
                .toList();
        Map<UUID, Placement<UserPoints>> placementMap = new HashMap<>();
        Placement<UserPoints> previousPlacement = null;
        UserPosition userPos = new UserPosition();
        for (ScoredUser scoredUser : scoredUsers) {
            Placement<UserPoints> placement;
            if (previousPlacement != null && previousPlacement.value().equals(scoredUser.score())) {
                placement = new Placement<>(previousPlacement.pos(), scoredUser.score());
            } else {
                placement = new Placement<>(userPos, scoredUser.score());
            }
            placementMap.put(scoredUser.id, placement);
            previousPlacement = placement;
            userPos = userPos.next();
        }
        return placementMap;
    }

    private List<RaceId> getSeasonRaceIds(Year year) {
        List<RaceId> raceIds = new ArrayList<>();
        raceIds.add(null);
        raceIds.addAll(raceService.getRaceIdsFinished(year));
        return raceIds;
    }

    private record ScoredUser(UUID id, UserPoints score) implements Comparable<ScoredUser> {

        @Override
        public int compareTo(ScoredUser o) {
            return score.compareTo(o.score);
        }
    }

}