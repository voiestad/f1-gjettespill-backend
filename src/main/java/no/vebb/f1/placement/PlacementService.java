package no.vebb.f1.placement;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.graph.GuesserPointsSeason;
import no.vebb.f1.placement.collection.PlacementGraphResult;
import no.vebb.f1.placement.collection.PlacementObj;
import no.vebb.f1.placement.collection.PositionResult;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.placement.domain.UserPosition;
import no.vebb.f1.placement.placementCategory.*;
import no.vebb.f1.placement.placementRace.*;
import no.vebb.f1.placement.placementYear.PlacementYearEntity;
import no.vebb.f1.placement.placementYear.PlacementYearRepository;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.util.collection.Guesser;
import no.vebb.f1.util.collection.Medals;
import no.vebb.f1.util.collection.Placement;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.util.collection.userTables.Summary;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.year.Year;
import no.vebb.f1.year.YearService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlacementService {

    private final YearService yearService;
    private final PlacementYearRepository placementYearRepository;
    private final PlacementRaceRepository placementRaceRepository;
    private final PlacementCategoryRepository placementCategoryRepository;
    private final PlacementRaceYearStartRepository placementRaceYearStartRepository;
    private final PlacementCategoryYearStartRepository placementCategoryYearStartRepository;

    public PlacementService(
            YearService yearService,
            PlacementYearRepository placementYearRepository,
            PlacementRaceRepository placementRaceRepository,
            PlacementCategoryRepository placementCategoryRepository,
            PlacementRaceYearStartRepository placementRaceYearStartRepository,
            PlacementCategoryYearStartRepository placementCategoryYearStartRepository
    ) {
        this.yearService = yearService;
        this.placementYearRepository = placementYearRepository;
        this.placementRaceRepository = placementRaceRepository;
        this.placementCategoryRepository = placementCategoryRepository;
        this.placementRaceYearStartRepository = placementRaceYearStartRepository;
        this.placementCategoryYearStartRepository = placementCategoryYearStartRepository;
    }

    public void finalizeYear(Year year) {
        if (yearService.isFinishedYear(year)) {
            return;
        }
        yearService.finalizeYear(year);
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
        Placement<UserPoints> winner = categories.get(Category.FIRST);
        Placement<UserPoints> tenth = categories.get(Category.TENTH);
        Placement<UserPoints> total =
                new Placement<>(totalRes.placement(),
                        totalRes.points());
        return new Summary(drivers, constructors, flag, winner, tenth, total);
    }

    public List<Placement<Year>> getPreviousPlacements(UUID userId) {
        return placementYearRepository.findByIdUserId(userId).stream()
                .map(row ->
                        new Placement<>(
                                row.placement(),
                                row.year()
                        ))
                .toList();
    }

    public Medals getMedals(UUID userId) {
        MedalCount gold = new MedalCount(placementYearRepository.countByPlacementAndIdUserId(new UserPosition(1), userId));
        MedalCount silver = new MedalCount(placementYearRepository.countByPlacementAndIdUserId(new UserPosition(2), userId));
        MedalCount bronze = new MedalCount(placementYearRepository.countByPlacementAndIdUserId(new UserPosition(3), userId));
        return new Medals(gold, silver, bronze);
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
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.FIRST, summary.winner().pos(), summary.winner().value()));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId, userId, Category.TENTH, summary.tenth().pos(), summary.tenth().value()));
            } else {
                placementRaceYearStartEntities.add(new PlacementRaceYearStartEntity(year, userId, summary.total().pos(), summary.total().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.DRIVER, summary.drivers().pos(), summary.drivers().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.CONSTRUCTOR, summary.constructors().pos(), summary.constructors().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.FLAG, summary.flag().pos(), summary.flag().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.FIRST, summary.winner().pos(), summary.winner().value()));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year, userId, Category.TENTH, summary.tenth().pos(), summary.tenth().value()));
            }
        }
        placementRaceRepository.saveAll(placementRaceEntities);
        placementCategoryRepository.saveAll(placementCategoryEntities);
        placementRaceYearStartRepository.saveAll(placementRaceYearStartEntities);
        placementCategoryYearStartRepository.saveAll(placementCategoryYearStartEntities);
    }

    public List<GuesserPointsSeason> getGraph(Year year) {
        Map<UUID, List<UserPoints>> userPoints = new LinkedHashMap<>();
        Map<UUID, String> usernames = new HashMap<>();
        for (PlacementGraphResult row : placementRaceRepository.findAllByYear(year.value)) {
            UUID id = row.getUserId();
            UserPoints points = new UserPoints(row.getPoints());
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
        List<PositionResult> races = placementRaceRepository.findAllByYearOrderByPosition(year);
        int maxPos = races.isEmpty() ? 0 : races.get(races.size() - 1).getPosition().toValue();
        return placementRaceRepository.getPlacementsByPositionAndYear(maxPos, year.value).stream()
                .map(row -> new RankedGuesser(
                        new Guesser(
                                row.getUsername(),
                                new UserPoints(row.getPoints()),
                                row.getUserId()
                        ), new UserPosition(row.getPlacement())
                ))
                .filter(RankedGuesser::hasPoints)
                .toList();
    }
}
