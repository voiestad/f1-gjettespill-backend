package no.vebb.f1.placement;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.graph.GuesserPointsSeason;
import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.util.collection.Guesser;
import no.vebb.f1.util.collection.Medals;
import no.vebb.f1.util.collection.Placement;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.util.collection.userTables.Summary;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.year.YearService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlacementService {

    private final GuessService guessService;
    private final YearService yearService;
    private final PlacementYearRepository placementYearRepository;
    private final PlacementRaceRepository placementRaceRepository;
    private final PlacementCategoryRepository placementCategoryRepository;
    private final PlacementRaceYearStartRepository placementRaceYearStartRepository;
    private final PlacementCategoryYearStartRepository placementCategoryYearStartRepository;

    public PlacementService(
            GuessService guessService,
            YearService yearService,
            PlacementYearRepository placementYearRepository,
            PlacementRaceRepository placementRaceRepository,
            PlacementCategoryRepository placementCategoryRepository,
            PlacementRaceYearStartRepository placementRaceYearStartRepository,
            PlacementCategoryYearStartRepository placementCategoryYearStartRepository
    ) {
        this.guessService = guessService;
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
                .map(guesser -> new PlacementYearEntity(year.value, guesser.guesser().id(), guesser.rank().value()))
                .toList();
        placementYearRepository.saveAll(placements);
    }

    public Summary getSummary(RaceId raceId, Year year, PublicUserDto user) {
        List<? extends PlacementCategory> categoriesRes = raceId != null ?
                placementCategoryRepository.findByIdRaceIdAndIdUserId(raceId.value, user.id()) :
                placementCategoryYearStartRepository.findByIdYearAndIdUserId(year.value, user.id());
        Optional<? extends PlacementRace> optTotalRes = raceId != null ?
                placementRaceRepository.findAllByIdRaceIdAndIdUserId(raceId.value, user.id()) :
                placementRaceYearStartRepository.findByIdYearAndIdUserId(year.value, user.id());
        if (optTotalRes.isEmpty() || categoriesRes.isEmpty()) {
            return null;
        }
        PlacementRace totalRes = optTotalRes.get();
        Map<Category, Placement<Points>> categories = new HashMap<>();
        for (PlacementCategory row : categoriesRes) {
            Category category = new Category(row.categoryName());
            Position pos = new Position(row.placement());
            Points points = new Points(row.points());
            Placement<Points> placement = new Placement<>(pos, points);
            categories.put(category, placement);
        }
        Placement<Points> drivers = categories.get(new Category("DRIVER", guessService));
        Placement<Points> constructors = categories.get(new Category("CONSTRUCTOR", guessService));
        Placement<Points> flag = categories.get(new Category("FLAG", guessService));
        Placement<Points> winner = categories.get(new Category("FIRST", guessService));
        Placement<Points> tenth = categories.get(new Category("TENTH", guessService));
        Placement<Points> total =
                new Placement<>(new Position(totalRes.placement()),
                        new Points(totalRes.points()));
        return new Summary(drivers, constructors, flag, winner, tenth, total);
    }

    public List<Placement<Year>> getPreviousPlacements(UUID userId) {
        return placementYearRepository.findByIdUserId(userId).stream()
                .map(row ->
                        new Placement<>(
                                new Position(row.placement()),
                                new Year(row.year())
                        ))
                .toList();
    }

    public Medals getMedals(UUID userId) {
        MedalCount gold = new MedalCount(placementYearRepository.countByPlacementAndIdUserId(1, userId));
        MedalCount silver = new MedalCount(placementYearRepository.countByPlacementAndIdUserId(1, userId));
        MedalCount bronze = new MedalCount(placementYearRepository.countByPlacementAndIdUserId(1, userId));
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
                placementRaceEntities.add(new PlacementRaceEntity(raceId.value, userId, summary.total().pos().value(), summary.total().value().value));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId.value, userId, new Category("DRIVER").value, summary.drivers().pos().value(), summary.drivers().value().value));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId.value, userId, new Category("CONSTRUCTOR").value, summary.constructors().pos().value(), summary.constructors().value().value));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId.value, userId, new Category("FLAG").value, summary.flag().pos().value(), summary.flag().value().value));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId.value, userId, new Category("FIRST").value, summary.winner().pos().value(), summary.winner().value().value));
                placementCategoryEntities.add(new PlacementCategoryEntity(raceId.value, userId, new Category("TENTH").value, summary.tenth().pos().value(), summary.tenth().value().value));
            } else {
                placementRaceYearStartEntities.add(new PlacementRaceYearStartEntity(year.value, userId, summary.total().pos().value(), summary.total().value().value));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year.value, userId, new Category("DRIVER").value, summary.drivers().pos().value(), summary.drivers().value().value));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year.value, userId, new Category("CONSTRUCTOR").value, summary.constructors().pos().value(), summary.constructors().value().value));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year.value, userId, new Category("FLAG").value, summary.flag().pos().value(), summary.flag().value().value));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year.value, userId, new Category("FIRST").value, summary.winner().pos().value(), summary.winner().value().value));
                placementCategoryYearStartEntities.add(new PlacementCategoryYearStartEntity(year.value, userId, new Category("TENTH").value, summary.tenth().pos().value(), summary.tenth().value().value));
            }
        }
        placementRaceRepository.saveAll(placementRaceEntities);
        placementCategoryRepository.saveAll(placementCategoryEntities);
        placementRaceYearStartRepository.saveAll(placementRaceYearStartEntities);
        placementCategoryYearStartRepository.saveAll(placementCategoryYearStartEntities);
    }

    public List<GuesserPointsSeason> getGraph(Year year) {
        Map<UUID, List<Points>> userPoints = new LinkedHashMap<>();
        Map<UUID, String> usernames = new HashMap<>();
        for (PlacementGraphResult row : placementRaceRepository.findAllByYear(year.value)) {
            UUID id = row.getUserId();
            Points points = new Points(row.getPoints());
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
        List<PositionResult> races = placementRaceRepository.findAllByYearOrderByPosition(year.value);
        int maxPos = races.isEmpty() ? 0 : races.get(races.size() - 1).getPosition();
        return placementRaceRepository.getPlacementsByPositionAndYear(maxPos, year.value).stream()
                .map(row -> new RankedGuesser(
                        new Guesser(
                                row.getUsername(),
                                new Points(row.getPoints()),
                                row.getUserId()
                        ), new Position(row.getPlacement())
                ))
                .filter(RankedGuesser::hasPoints)
                .toList();
    }
}
