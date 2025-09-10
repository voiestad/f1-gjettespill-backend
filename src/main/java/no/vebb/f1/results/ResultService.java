package no.vebb.f1.results;

import no.vebb.f1.competitors.constructor.ConstructorYearEntity;
import no.vebb.f1.competitors.constructor.ConstructorYearRepository;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.competitors.driver.DriverYearEntity;
import no.vebb.f1.competitors.driver.DriverYearRepository;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.vebb.f1.results.constructorStandings.ConstructorStandingsRepository;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.domain.CompetitorPosition;
import no.vebb.f1.results.driverStandings.DriverStandingsEntity;
import no.vebb.f1.results.driverStandings.DriverStandingsRepository;
import no.vebb.f1.results.raceResult.RaceResultEntity;
import no.vebb.f1.results.raceResult.RaceResultRepository;
import no.vebb.f1.results.startingGrid.StartingGridEntity;
import no.vebb.f1.results.startingGrid.StartingGridRepository;
import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {

    private final StartingGridRepository startingGridRepository;
    private final RaceResultRepository raceResultRepository;
    private final DriverStandingsRepository driverStandingsRepository;
    private final ConstructorStandingsRepository constructorStandingsRepository;
    private final DriverYearRepository driverYearRepository;
    private final ConstructorYearRepository constructorYearRepository;

    public ResultService(StartingGridRepository startingGridRepository, RaceResultRepository raceResultRepository, DriverStandingsRepository driverStandingsRepository, ConstructorStandingsRepository constructorStandingsRepository, DriverYearRepository driverYearRepository, ConstructorYearRepository constructorYearRepository) {
        this.startingGridRepository = startingGridRepository;
        this.raceResultRepository = raceResultRepository;
        this.driverStandingsRepository = driverStandingsRepository;
        this.constructorStandingsRepository = constructorStandingsRepository;
        this.driverYearRepository = driverYearRepository;
        this.constructorYearRepository = constructorYearRepository;
    }

    public List<StartingGridEntity> getStartingGrid(RaceId raceId) {
        return startingGridRepository.findAllByIdRaceIdOrderByPosition(raceId);
    }

    public List<RaceResultEntity> getRaceResult(RaceId raceId) {
        return raceResultRepository.findAllByIdRaceIdOrderByIdFinishingPosition(raceId);
    }

    public List<DriverStandingsEntity> getDriverStandings(RaceId raceId) {
        return driverStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId);
    }

    public List<ConstructorStandingsEntity> getConstructorStandings(RaceId raceId) {
        return constructorStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId);
    }


    public void insertDriverStartingGrid(RaceId raceId, CompetitorPosition position, Driver driver) {
        StartingGridEntity startingGridEntity = new StartingGridEntity(raceId, driver, position);
        startingGridRepository.save(startingGridEntity);
    }

    public void insertDriverRaceResult(RaceId raceId, String position, Driver driver, CompetitorPoints points, CompetitorPosition finishingPosition) {
        RaceResultEntity raceResultEntity = new RaceResultEntity(raceId, finishingPosition, position, driver, points);
        raceResultRepository.save(raceResultEntity);
    }


    public void insertDriverIntoStandings(RaceId raceId, Driver driver, CompetitorPosition position, CompetitorPoints points) {
        DriverStandingsEntity driverStandingsEntity = new DriverStandingsEntity(raceId, driver, position, points);
        driverStandingsRepository.save(driverStandingsEntity);
    }


    public void insertConstructorIntoStandings(RaceId raceId, Constructor constructor, CompetitorPosition position, CompetitorPoints points) {
        ConstructorStandingsEntity constructorStandingsEntity = new ConstructorStandingsEntity(raceId, constructor, position, points);
        constructorStandingsRepository.save(constructorStandingsEntity);
    }

    public boolean isStartingGridAdded(RaceId raceId) {
        return startingGridRepository.existsByIdRaceId(raceId);
    }

    public boolean isRaceResultAdded(RaceId raceId) {
        return raceResultRepository.existsByIdRaceId(raceId);
    }

    public RaceId getCurrentRaceIdToGuess() throws NoAvailableRaceException {
        List<StartingGridEntity> startingGridEntities = startingGridRepository.findAllByNotInRaceResult();
        if (startingGridEntities.isEmpty()) {
            throw new NoAvailableRaceException("No starting grid found that is not in race result");
        }
        if (startingGridEntities.size() > 1) {
            throw new NoAvailableRaceException("Too many starting grids found");
        }
        return startingGridEntities.get(0).raceId();
    }

    public List<Driver> getDriverStandings(RaceId raceId, Year year) {
        if (raceId == null) {
            return driverYearRepository.findAllByIdYearOrderByPosition(year).stream()
                    .map(DriverYearEntity::driverName)
                    .toList();
        }
        return driverStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId).stream()
                .map(DriverStandingsEntity::driverName)
                .toList();

    }

    public List<Constructor> getConstructorStandings(RaceId raceId, Year year) {
        if (raceId == null) {
            return constructorYearRepository.findAllByIdYearOrderByPosition(year).stream()
                    .map(ConstructorYearEntity::constructorName)
                    .toList();
        }
        return constructorStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId).stream()
                .map(ConstructorStandingsEntity::constructorName)
                .toList();
    }

    public List<Driver> getDriversFromStartingGrid(RaceId raceId) {
        return getStartingGrid(raceId).stream()
                .map(StartingGridEntity::driverName)
                .toList();
    }

    public List<ColoredCompetitor<Driver>> getDriversFromStartingGridWithColors(RaceId raceId) {
        return startingGridRepository.findAllByRaceIdWithColor(raceId).stream()
                .map(ColoredCompetitor::fromIColoredCompetitorToDriver)
                .toList();
    }

}
