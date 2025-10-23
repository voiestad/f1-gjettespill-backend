package no.voiestad.f1.guessing;

import java.util.*;

import no.voiestad.f1.collection.*;
import no.voiestad.f1.competitors.constructor.*;
import no.voiestad.f1.competitors.domain.*;
import no.voiestad.f1.competitors.driver.*;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.guessing.collection.*;
import no.voiestad.f1.guessing.constructor.*;
import no.voiestad.f1.guessing.driver.*;
import no.voiestad.f1.guessing.driverPlace.*;
import no.voiestad.f1.guessing.flag.*;
import no.voiestad.f1.guessing.collection.PlaceGuessData;
import no.voiestad.f1.race.*;
import no.voiestad.f1.stats.domain.Flag;
import no.voiestad.f1.user.*;
import no.voiestad.f1.year.Year;

import org.springframework.stereotype.Service;

@Service
public class GuessService {

    private final UserRespository userRespository;
    private final ConstructorGuessRepository constructorGuessRepository;
    private final DriverGuessRepository driverGuessRepository;
    private final FlagGuessRepository flagGuessRepository;
    private final DriverPlaceGuessRepository driverPlaceGuessRepository;
    private final DriverRepository driverRepository;
    private final ConstructorRepository constructorRepository;

    public GuessService(
            UserRespository userRespository,
            ConstructorGuessRepository constructorGuessRepository,
            DriverGuessRepository driverGuessRepository,
            FlagGuessRepository flagGuessRepository,
            DriverPlaceGuessRepository driverPlaceGuessRepository,
            DriverRepository driverRepository,
            ConstructorRepository constructorRepository) {
        this.userRespository = userRespository;
        this.constructorGuessRepository = constructorGuessRepository;
        this.driverGuessRepository = driverGuessRepository;
        this.flagGuessRepository = flagGuessRepository;
        this.driverPlaceGuessRepository = driverPlaceGuessRepository;
        this.driverRepository = driverRepository;
        this.constructorRepository = constructorRepository;
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    public List<IFlagGuessed> getDataForFlagTable(RacePosition racePos, Year year, UUID userId) {
        return (racePos == null ? flagGuessRepository.findAllByUserIdAndYear(userId, year) :
                flagGuessRepository.findAllByUserIdAndYearAndPosition(userId, year, racePos));
    }

    public List<IUserRaceGuessTable> getDataForPlaceGuessTable(
            Category category, UUID userId, Year year, RacePosition racePos) {
        return driverPlaceGuessRepository.findAllByCategoryNameAndYearAndPositionAndUserIdOrderByPosition(
                category, year, racePos, userId
        );
    }

    public List<DriverEntity> getGuessedYearDriver(Year year, UUID userId) {
        return driverGuessRepository.findAllByIdYearAndIdUserIdOrderByIdPosition(year, userId).stream()
                .map(DriverGuessEntity::driver)
                .toList();
    }

    public List<ConstructorEntity> getGuessedYearConstructor(Year year, UUID userId) {
        return constructorGuessRepository.findAllByIdYearAndIdUserIdOrderByIdPosition(year, userId).stream()
                .map(ConstructorGuessEntity::constructor)
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

    public Optional<Flags> getFlagGuesses(UUID userId, Year year) {
        List<FlagGuessEntity> flagGuesses = flagGuessRepository.findAllByIdUserIdAndIdYear(userId, year);
        Map<Flag, Integer> map = new HashMap<>();
        for (FlagGuessEntity flagGuess : flagGuesses) {
            map.put(flagGuess.flagName(), flagGuess.amount());
        }
        return Flags.getFlags(
                map.getOrDefault(Flag.YELLOW_FLAG, 0),
                map.getOrDefault(Flag.RED_FLAG, 0),
                map.getOrDefault(Flag.SAFETY_CAR, 0));
    }

    public DriverId getGuessedDriverPlace(RaceId raceId, Category category, UUID userId) {
        return driverPlaceGuessRepository.findById(new DriverPlaceGuessId(userId, raceId, category))
                .map(DriverPlaceGuessEntity::driver)
                .map(DriverEntity::driverId)
                .orElse(null);
    }

    public void addDriverPlaceGuess(UUID userId, RaceId raceId, DriverEntity driver, Category category) {
        driverPlaceGuessRepository.save(new DriverPlaceGuessEntity(userId, raceId, category, driver));
    }

    public List<CompetitorDTO> getDriversGuess(UUID userId, Year year) {
        List<CompetitorDTO> guessed = driverGuessRepository
                .findAllByIdYearAndIdUserIdOrderByIdPosition(year, userId).stream()
                .map(DriverGuessEntity::driver)
                .map(CompetitorDTO::fromEntity)
                .toList();
        List<CompetitorDTO> driversYear = driverRepository.findAllByYearOrderByPosition(year).stream()
                .map(CompetitorDTO::fromEntity)
                .toList();
        return appendNotGuessed(guessed, driversYear);
    }

    public List<CompetitorDTO> getConstructorsGuess(UUID userId, Year year) {
        List<CompetitorDTO> guessed = constructorGuessRepository
                .findAllByIdYearAndIdUserIdOrderByIdPosition(year, userId).stream()
                .map(ConstructorGuessEntity::constructor)
                .map(CompetitorDTO::fromEntity)
                .toList();
        List<CompetitorDTO> constructorsYear = constructorRepository.findAllByYearOrderByPosition(year).stream()
                .map(CompetitorDTO::fromEntity)
                .toList();
        return appendNotGuessed(guessed, constructorsYear);
    }

    private List<CompetitorDTO> appendNotGuessed(List<CompetitorDTO> guessed, List<CompetitorDTO> competitorsYear) {
        List<CompetitorDTO> result = new ArrayList<>(guessed);
        Set<CompetitorDTO> competitors = new HashSet<>(guessed);
        for (CompetitorDTO competitor : competitorsYear) {
            if (!competitors.contains(competitor)) {
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

    public List<CompetitorGuessYear<DriverName>> userGuessDataDriver(UUID userId) {
        return driverGuessRepository.findAllByIdUserIdOrderByIdYearDescIdPosition(userId).stream()
                .map(CompetitorGuessYear::fromEntity)
                .toList();
    }

    public List<CompetitorGuessYear<ConstructorName>> userGuessDataConstructor(UUID userId) {
        return constructorGuessRepository.findAllByIdUserIdOrderByIdYearDescIdPosition(userId).stream()
                .map(CompetitorGuessYear::fromEntity)
                .toList();
    }

    public List<FlagGuessYear> userGuessDataFlag(UUID userId) {
        return flagGuessRepository.findAllByIdUserIdOrderByIdYearDescIdFlagName(userId).stream()
                .map(FlagGuessYear::fromEntity)
                .toList();

    }

    public List<PlaceGuessData> userGuessDataDriverPlace(UUID userId) {
        return driverPlaceGuessRepository.findAllByUserId(userId).stream()
                .map(PlaceGuessData::fromIPlaceGuess)
                .toList();
    }
}
