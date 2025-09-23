package no.vebb.f1.guessing;

import no.vebb.f1.collection.*;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.constructor.ConstructorYearRepository;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.competitors.driver.DriverYearRepository;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.guessing.collection.IFlagGuessed;
import no.vebb.f1.guessing.collection.IUserRaceGuessTable;
import no.vebb.f1.guessing.collection.UserRaceGuess;
import no.vebb.f1.guessing.constructor.ConstructorGuessEntity;
import no.vebb.f1.guessing.constructor.ConstructorGuessRepository;
import no.vebb.f1.guessing.driver.DriverGuessEntity;
import no.vebb.f1.guessing.driver.DriverGuessRepository;
import no.vebb.f1.guessing.driverPlace.DriverPlaceGuessEntity;
import no.vebb.f1.guessing.driverPlace.DriverPlaceGuessId;
import no.vebb.f1.guessing.driverPlace.DriverPlaceGuessRepository;
import no.vebb.f1.guessing.flag.FlagGuessEntity;
import no.vebb.f1.guessing.flag.FlagGuessRepository;
import no.vebb.f1.guessing.collection.PlaceGuess;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.results.collection.IColoredCompetitor;
import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserRespository;
import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GuessService {

    private final UserRespository userRespository;
    private final ConstructorGuessRepository constructorGuessRepository;
    private final DriverGuessRepository driverGuessRepository;
    private final FlagGuessRepository flagGuessRepository;
    private final DriverPlaceGuessRepository driverPlaceGuessRepository;
    private final DriverYearRepository driverYearRepository;
    private final ConstructorYearRepository constructorYearRepository;

    public GuessService(UserRespository userRespository, ConstructorGuessRepository constructorGuessRepository, DriverGuessRepository driverGuessRepository, FlagGuessRepository flagGuessRepository, DriverPlaceGuessRepository driverPlaceGuessRepository, DriverYearRepository driverYearRepository, ConstructorYearRepository constructorYearRepository) {
        this.userRespository = userRespository;
        this.constructorGuessRepository = constructorGuessRepository;
        this.driverGuessRepository = driverGuessRepository;
        this.flagGuessRepository = flagGuessRepository;
        this.driverPlaceGuessRepository = driverPlaceGuessRepository;
        this.driverYearRepository = driverYearRepository;
        this.constructorYearRepository = constructorYearRepository;
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    public List<IFlagGuessed> getDataForFlagTable(RacePosition racePos, Year year, UUID userId) {
        return (racePos == null ? flagGuessRepository.findAllByUserIdAndYear(userId, year) :
                flagGuessRepository.findAllByUserIdAndYearAndPosition(userId, year, racePos));
    }

    public List<IUserRaceGuessTable> getDataForPlaceGuessTable(Category category, UUID userId, Year year, RacePosition racePos) {
        return driverPlaceGuessRepository.findAllByCategoryNameAndYearAndPositionAndUserIdOrderByPosition(
                category, year, racePos, userId
        );
    }

    public List<Driver> getGuessedYearDriver(Year year, UUID userId) {
        return driverGuessRepository.findAllByIdYearAndIdUserIdOrderByIdPosition(year, userId).stream()
                .map(DriverGuessEntity::driverName)
                .toList();
    }

    public List<Constructor> getGuessedYearConstructor(Year year, UUID userId) {
        return constructorGuessRepository.findAllByIdYearAndIdUserIdOrderByIdPosition(year, userId).stream()
                .map(ConstructorGuessEntity::constructorName)
                .toList();
    }

    public List<UserRaceGuess> getUserGuessesDriverPlace(RaceId raceId, Category category) {
        return driverPlaceGuessRepository.findAllByRaceIdAndCategoryNameOrderByUsername(category, raceId).stream()
                .map(UserRaceGuess::fromIUserRaceGuess)
                .toList();
    }

    public void addFlagGuesses(UUID userId, Year year, Flags flags) {
        flagGuessRepository.saveAll(Arrays.asList(
                new FlagGuessEntity(userId, Flag.YELLOW_FLAG, year, flags.yellow),
                new FlagGuessEntity(userId, Flag.RED_FLAG, year, flags.red),
                new FlagGuessEntity(userId, Flag.SAFETY_CAR, year, flags.safetyCar)
        ));
    }

    public Flags getFlagGuesses(UUID userId, Year year) {
        Flags flags = new Flags();
        List<FlagGuessEntity> flagGuesses = flagGuessRepository.findAllByIdUserIdAndIdYear(userId, year);
        for (FlagGuessEntity flagGuess : flagGuesses) {
            switch (flagGuess.flagName()) {
                case YELLOW_FLAG:
                    flags.yellow = flagGuess.amount();
                    break;
                case RED_FLAG:
                    flags.red = flagGuess.amount();
                    break;
                case SAFETY_CAR:
                    flags.safetyCar = flagGuess.amount();
                    break;
            }
        }
        return flags;
    }

    public Driver getGuessedDriverPlace(RaceId raceId, Category category, UUID userId) {
        return driverPlaceGuessRepository.findById(new DriverPlaceGuessId(userId, raceId, category))
                .map(DriverPlaceGuessEntity::driverName)
                .orElse(null);
    }

    public void addDriverPlaceGuess(UUID userId, RaceId raceId, Driver driver, Category category) {
        driverPlaceGuessRepository.save(new DriverPlaceGuessEntity(userId, raceId, category, driver));
    }

    public List<ColoredCompetitor<Driver>> getDriversGuess(UUID userId, Year year) {
        List<IColoredCompetitor> guessed = driverGuessRepository.findAllByUserIdOrderByIdPosition(userId, year);
        List<IColoredCompetitor> driversYear = driverYearRepository.findAllByYearOrderByPositionWithColor(year);
        List<IColoredCompetitor> result = appendNotGuessed(guessed, driversYear);
        return result.stream()
                .map(ColoredCompetitor::fromIColoredCompetitorToDriver)
                .toList();
    }

    public List<ColoredCompetitor<Constructor>> getConstructorsGuess(UUID userId, Year year) {
        List<IColoredCompetitor> guessed = constructorGuessRepository.findAllByUserIdOrderByIdPosition(userId, year);
        List<IColoredCompetitor> constructorsYear = constructorYearRepository.findAllByYearOrderByPosition(year);
        List<IColoredCompetitor> result = appendNotGuessed(guessed, constructorsYear);
        return result.stream()
                .map(ColoredCompetitor::fromIColoredCompetitorToConstructor)
                .toList();
    }

    private List<IColoredCompetitor> appendNotGuessed(List<IColoredCompetitor> guessed, List<IColoredCompetitor> competitorsYear) {
        List<IColoredCompetitor> result = new ArrayList<>(guessed);
        Set<String> competitorNames = new HashSet<>(guessed.stream().map(IColoredCompetitor::getCompetitorName).toList());
        for (IColoredCompetitor competitor : competitorsYear) {
            if (!competitorNames.contains(competitor.getCompetitorName())) {
                result.add(competitor);
            }
        }
        return result;
    }

    public void addDriversYearGuesses(List<DriverGuessEntity> driverGuesses) {
        driverGuessRepository.saveAll(driverGuesses);
    }

    public void addConstructorsYearGuesses(List<ConstructorGuessEntity> constructorGuesses) {
        constructorGuessRepository.saveAll(constructorGuesses);
    }

    public List<UserEntity> getSeasonGuessers(Year year) {
        return userRespository.findAllByGuessedYear(year);
    }

    public List<CompetitorGuessYear<Driver>> userGuessDataDriver(UUID userId) {
        return driverGuessRepository.findAllByIdUserIdOrderByIdYearDescIdPosition(userId).stream()
                .map(CompetitorGuessYear::fromEntity)
                .toList();
    }

    public List<CompetitorGuessYear<Constructor>> userGuessDataConstructor(UUID userId) {
        return constructorGuessRepository.findAllByIdUserIdOrderByIdYearDescIdPosition(userId).stream()
                .map(CompetitorGuessYear::fromEntity)
                .toList();
    }

    public List<FlagGuessYear> userGuessDataFlag(UUID userId) {
        return flagGuessRepository.findAllByIdUserIdOrderByIdYearDescIdFlagName(userId).stream()
                .map(FlagGuessYear::fromEntity)
                .toList();

    }

    public List<PlaceGuess> userGuessDataDriverPlace(UUID userId) {
        return driverPlaceGuessRepository.findAllByUserId(userId).stream()
                .map(PlaceGuess::fromIPlaceGuess)
                .toList();
    }
}
