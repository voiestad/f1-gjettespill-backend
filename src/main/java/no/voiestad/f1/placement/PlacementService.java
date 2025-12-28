package no.voiestad.f1.placement;

import java.util.*;

import no.voiestad.f1.cutoff.CutoffService;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.league.LeagueService;
import no.voiestad.f1.placement.collection.*;
import no.voiestad.f1.placement.domain.*;
import no.voiestad.f1.placement.placementCategory.*;
import no.voiestad.f1.placement.placementRace.*;
import no.voiestad.f1.placement.placementYear.*;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.scoring.UserPlacementStats;
import no.voiestad.f1.user.PublicUserDto;
import no.voiestad.f1.collection.*;
import no.voiestad.f1.scoring.userTables.Summary;
import no.voiestad.f1.year.Year;
import no.voiestad.f1.year.YearService;

import org.springframework.stereotype.Service;

@Service
public class PlacementService {

    private final YearService yearService;
    private final PlacementYearRepository placementYearRepository;
    private final PlacementRaceRepository placementRaceRepository;
    private final PlacementCategoryRepository placementCategoryRepository;
    private final PlacementRaceYearStartRepository placementRaceYearStartRepository;
    private final PlacementCategoryYearStartRepository placementCategoryYearStartRepository;
    private final CutoffService cutoffService;
    private final LeagueService leagueService;

    public PlacementService(
            YearService yearService,
            PlacementYearRepository placementYearRepository,
            PlacementRaceRepository placementRaceRepository,
            PlacementCategoryRepository placementCategoryRepository,
            PlacementRaceYearStartRepository placementRaceYearStartRepository,
            PlacementCategoryYearStartRepository placementCategoryYearStartRepository,
            CutoffService cutoffService, LeagueService leagueService) {
        this.yearService = yearService;
        this.placementYearRepository = placementYearRepository;
        this.placementRaceRepository = placementRaceRepository;
        this.placementCategoryRepository = placementCategoryRepository;
        this.placementRaceYearStartRepository = placementRaceYearStartRepository;
        this.placementCategoryYearStartRepository = placementCategoryYearStartRepository;
        this.cutoffService = cutoffService;
        this.leagueService = leagueService;
    }

    public void finalizeYear(Year year) {
        if (yearService.isFinishedYear(year)) {
            return;
        }
        yearService.finalizeYear(year);
        leagueService.clearInvitationsByYear(year);
        List<PlacementYearEntity> placements = getLeaderboard(year).stream()
                .map(guesser -> new PlacementYearEntity(year, guesser.guesser().id(), guesser.rank()))
                .toList();
        placementYearRepository.saveAll(placements);
    }

    public Summary getSummary(RaceId raceId, Year year, PublicUserDto user) {
        List<? extends PlacementCategory> categoriesRes = raceId != null ?
                placementCategoryRepository.findByIdRaceIdAndIdUserId(raceId, user.id()) :
                placementCategoryYearStartRepository.findByIdYearAndIdUserId(year, user.id());
        Optional<? extends PlacementRace> optTotalRes = raceId != null ?
                placementRaceRepository.findAllByIdRaceIdAndIdUserId(raceId, user.id()) :
                placementRaceYearStartRepository.findByIdYearAndIdUserId(year, user.id());
        if (optTotalRes.isEmpty() || categoriesRes.isEmpty()) {
            return null;
        }
        PlacementRace totalRes = optTotalRes.get();
        Map<Category, Placement<UserPoints>> categories = new HashMap<>();
        for (PlacementCategory row : categoriesRes) {
            Category category = row.categoryName();
            UserPosition pos = row.placement();
            UserPoints points = row.points();
            Placement<UserPoints> placement = new Placement<>(pos, points);
            categories.put(category, placement);
        }
        Placement<UserPoints> drivers = categories.get(Category.DRIVER);
        Placement<UserPoints> constructors = categories.get(Category.CONSTRUCTOR);
        Placement<UserPoints> flag = categories.get(Category.FLAG);
        Placement<UserPoints> first = categories.get(Category.FIRST);
        Placement<UserPoints> tenth = categories.get(Category.TENTH);
        Placement<UserPoints> pole = categories.get(Category.POLE);
        Placement<UserPoints> total =
                new Placement<>(totalRes.placement(),
                        totalRes.points());
        return new Summary(drivers, constructors, flag, first, tenth, pole, total);
    }

    public List<Placement<Year>> getPreviousPlacements(UUID userId) {
        return placementYearRepository.findByIdUserIdOrderByIdYearDesc(userId).stream()
                .map(row ->
                        new Placement<>(
                                row.placement(),
                                row.year()
                        ))
                .toList();
    }

    public Medals getMedals(UUID userId) {
        return new Medals(
                placementYearRepository.countByPlacementAndIdUserId(UserPosition.getUserPosition(1).orElseThrow(RuntimeException::new), userId),
                placementYearRepository.countByPlacementAndIdUserId(UserPosition.getUserPosition(2).orElseThrow(RuntimeException::new), userId),
                placementYearRepository.countByPlacementAndIdUserId(UserPosition.getUserPosition(3).orElseThrow(RuntimeException::new), userId)
        );
    }

    public void addUserScores(List<PlacementObj> placementObjs) {
        List<PlacementRaceEntity> placementRaceEntities = new ArrayList<>();
        List<PlacementCategoryEntity> placementCategoryEntities = new ArrayList<>();
        List<PlacementRaceYearStartEntity> placementRaceYearStartEntities = new ArrayList<>();
        List<PlacementCategoryYearStartEntity> placementCategoryYearStartEntities = new ArrayList<>();
        for (PlacementObj placementObj : placementObjs) {
            UUID userId = placementObj.userId();
            Summary summary = placementObj.summary();
            RaceId raceId = placementObj.raceId();
            Year year = placementObj.year();
            if (raceId != null) {
                placementRaceEntities.add(new PlacementRaceEntity(raceId, userId, summary.total().pos(), summary.total().value()));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.DRIVER, summary.drivers().pos(), summary.drivers().value()));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.CONSTRUCTOR, summary.constructors().pos(), summary.constructors().value()));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.FLAG, summary.flag().pos(), summary.flag().value()));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.FIRST, summary.first().pos(), summary.first().value()));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.TENTH, summary.tenth().pos(), summary.tenth().value()));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.POLE, summary.pole().pos(), summary.pole().value()));
            } else {
                placementRaceYearStartEntities.add(new PlacementRaceYearStartEntity(year, userId, summary.total().pos(), summary.total().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.DRIVER, summary.drivers().pos(), summary.drivers().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.CONSTRUCTOR, summary.constructors().pos(), summary.constructors().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.FLAG, summary.flag().pos(), summary.flag().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.FIRST, summary.first().pos(), summary.first().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.TENTH, summary.tenth().pos(), summary.tenth().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.POLE, summary.pole().pos(), summary.pole().value()));
            }
        }
        placementRaceRepository.saveAll(placementRaceEntities);
        placementCategoryRepository.saveAll(placementCategoryEntities);
        placementRaceYearStartRepository.saveAll(placementRaceYearStartEntities);
        placementCategoryYearStartRepository.saveAll(placementCategoryYearStartEntities);
    }

    public List<GuesserPointsSeason> getGraph(Year year) {
        if (cutoffService.isAbleToGuessYear(year)) {
            return null;
        }
        Map<UUID, List<UserPoints>> userPoints = new LinkedHashMap<>();
        Map<UUID, String> usernames = new HashMap<>();
        for (PlacementGraphResult row : placementRaceRepository.findAllByYear(year.value)) {
            UUID id = row.getUserId();
            UserPoints points = UserPoints.getUserPoints(row.getPoints()).orElseThrow(RuntimeException::new);
            if (!userPoints.containsKey(id)) {
                usernames.put(id, row.getUsername());
                userPoints.put(id, new ArrayList<>());
            }
            userPoints.get(id).add(points);
        }
        return userPoints.entrySet().stream()
                .map(entry -> new GuesserPointsSeason(usernames.get(entry.getKey()), entry.getValue()))
                .filter(GuesserPointsSeason::hasPoints)
                .toList();
    }

    public List<RankedGuesser> getLeaderboard(Year year) {
        if (cutoffService.isAbleToGuessYear(year)) {
            return null;
        }
        List<PositionResult> races = placementRaceRepository.findAllByYearOrderByPosition(year);
        int maxPos = races.isEmpty() ? 0 : races.get(races.size() - 1).getPosition().toValue();
        return placementRaceRepository.getPlacementsByPositionAndYear(maxPos, year.value).stream()
                .map(row -> new RankedGuesser(
                        new Guesser(
                                row.getUsername(),
                                UserPoints.getUserPoints(row.getPoints()).orElseThrow(RuntimeException::new),
                                row.getUserId()
                        ), UserPosition.getUserPosition(row.getPlacement()).orElseThrow(RuntimeException::new)
                ))
                .filter(RankedGuesser::hasPoints)
                .toList();
    }

    public UserPlacementStats getPlacementsStats(UUID userId, String username) {
        return new UserPlacementStats(username, getPreviousPlacements(userId), getMedals(userId));
    }
}
