package no.voiestad.f1.scoring;

import java.util.*;
import java.util.function.Function;

import no.voiestad.f1.guessing.GuessService;
import no.voiestad.f1.league.LeagueService;
import no.voiestad.f1.league.leagues.LeagueEntity;
import no.voiestad.f1.placement.collection.PlacementObj;
import no.voiestad.f1.placement.PlacementService;
import no.voiestad.f1.placement.collection.PlacementObjLeague;
import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.race.RaceService;
import no.voiestad.f1.results.ResultService;
import no.voiestad.f1.user.PublicUserDto;
import no.voiestad.f1.user.UserEntity;
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
    private final YearService yearService;

    private static final Logger logger = LoggerFactory.getLogger(ScoreCalculator.class);
    private final RaceService raceService;
    private final PlacementService placementService;
    private final GuessService guessService;
    private final ScoreService scoreService;
    private final ResultService resultService;
    private final UserService userService;
    private final LeagueService leagueService;

    public ScoreCalculator(YearService yearService, RaceService raceService, PlacementService placementService, GuessService guessService, ScoreService scoreService, ResultService resultService, UserService userService, LeagueService leagueService) {
        this.yearService = yearService;
        this.raceService = raceService;
        this.placementService = placementService;
        this.guessService = guessService;
        this.scoreService = scoreService;
        this.resultService = resultService;
        this.userService = userService;
        this.leagueService = leagueService;
    }

    @Transactional
    @Scheduled(initialDelay = TimeUtil.MINUTE)
    public void calculateScores() {
        logger.info("Calculating scores");
        Optional<Year> optYear = yearService.getCurrentYear();
        if (optYear.isEmpty()) {
            logger.warn("Did not calculate scores. Year not available.");
            return;
        }
        Year year = optYear.get();
        logger.info("Calculating general scores");
        calculate(year);
        logger.info("Finished calculating scores");
        logger.info("Calculating league scores");
        calculateLeagues(year);
        logger.info("Finished calculating league scores");
    }

    private void calculate(Year year) {
        List<UserEntity> guessers = userService.getAllUsers();
        List<RaceId> raceIds = getSeasonRaceIds(year);
        List<PlacementObj> placementObjs = new ArrayList<>();
        for (RaceId raceId : raceIds) {
            List<UserScore> userScores = guessers.stream()
                    .map(guesser -> new UserScore(PublicUserDto.fromEntity(guesser), year, raceId, raceService, guessService, scoreService, resultService))
                    .toList();
            Map<UUID, Placement<UserPoints>> driversPoints = getPlacementMap(userScores, UserScore::getDriversScore);
            Map<UUID, Placement<UserPoints>> constructorsPoints = getPlacementMap(userScores, UserScore::getConstructorsScore);
            Map<UUID, Placement<UserPoints>> flagPoints = getPlacementMap(userScores, UserScore::getFlagScore);
            Map<UUID, Placement<UserPoints>> firstPoints = getPlacementMap(userScores, UserScore::getFirstScore);
            Map<UUID, Placement<UserPoints>> tenthPoints = getPlacementMap(userScores, UserScore::getTenthScore);
            Map<UUID, Placement<UserPoints>> polePoints = getPlacementMap(userScores, UserScore::getPoleScore);
            Map<UUID, Placement<UserPoints>> totalPoints = getPlacementMap(userScores, UserScore::getScore);

            for (UserEntity guesser : guessers) {
                UUID id = guesser.id();
                Summary summary = new Summary(
                        driversPoints.get(id),
                        constructorsPoints.get(id),
                        flagPoints.get(id),
                        firstPoints.get(id),
                        tenthPoints.get(id),
                        polePoints.get(id),
                        totalPoints.get(id)
                );
                placementObjs.add(new PlacementObj(id, summary, raceId, year));
            }
        }
        placementService.addUserScores(placementObjs);
    }

    private void calculateLeagues(Year year) {
        List<PlacementObjLeague> placementObjs = new ArrayList<>();
        for (LeagueEntity league : leagueService.getLeagues(year)) {
            placementObjs.addAll(getObjsForLeague(league.leagueId(), year));
        }
        placementService.addUserLeagueScores(placementObjs);
    }

    public void calculateLeague(UUID leagueId, Year year) {
        placementService.addUserLeagueScores(getObjsForLeague(leagueId, year));
    }

    private List<PlacementObjLeague> getObjsForLeague(UUID leagueId, Year year) {
        List<PlacementObjLeague> placementObjs = new ArrayList<>();
        List<UserEntity> members = leagueService.getMembers(leagueId);
        List<RaceId> raceIds = getSeasonRaceIds(year);
        for (RaceId raceId : raceIds) {
            List<UserScore> userScores = members.stream()
                    .map(guesser -> new UserScore(PublicUserDto.fromEntity(guesser), year, raceId, raceService, guessService, scoreService, resultService))
                    .toList();
            Map<UUID, Placement<UserPoints>> driversPoints = getPlacementMap(userScores, UserScore::getDriversScore);
            Map<UUID, Placement<UserPoints>> constructorsPoints = getPlacementMap(userScores, UserScore::getConstructorsScore);
            Map<UUID, Placement<UserPoints>> flagPoints = getPlacementMap(userScores, UserScore::getFlagScore);
            Map<UUID, Placement<UserPoints>> firstPoints = getPlacementMap(userScores, UserScore::getFirstScore);
            Map<UUID, Placement<UserPoints>> tenthPoints = getPlacementMap(userScores, UserScore::getTenthScore);
            Map<UUID, Placement<UserPoints>> polePoints = getPlacementMap(userScores, UserScore::getPoleScore);
            Map<UUID, Placement<UserPoints>> totalPoints = getPlacementMap(userScores, UserScore::getScore);

            for (UserEntity guesser : members) {
                UUID id = guesser.id();
                Summary summary = new Summary(
                        driversPoints.get(id),
                        constructorsPoints.get(id),
                        flagPoints.get(id),
                        firstPoints.get(id),
                        tenthPoints.get(id),
                        polePoints.get(id),
                        totalPoints.get(id)
                );
                placementObjs.add(new PlacementObjLeague(id, summary, raceId, year, leagueId));
            }
        }
        return placementObjs;
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