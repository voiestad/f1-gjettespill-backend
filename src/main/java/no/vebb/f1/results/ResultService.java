package no.vebb.f1.results;

import no.vebb.f1.competitors.ConstructorYearEntity;
import no.vebb.f1.competitors.ConstructorYearRepository;
import no.vebb.f1.competitors.DriverYearEntity;
import no.vebb.f1.competitors.DriverYearRepository;
import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.util.exception.NoAvailableRaceException;
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
        return startingGridRepository.findAllByIdRaceIdOrderByPosition(raceId.value);
    }

    public List<RaceResultEntity> getRaceResult(RaceId raceId) {
        return raceResultRepository.findAllByIdRaceIdOrderByIdFinishingPosition(raceId.value);
    }

    public List<DriverStandingsEntity> getDriverStandings(RaceId raceId) {
        return driverStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId.value);
    }

    public List<ConstructorStandingsEntity> getConstructorStandings(RaceId raceId) {
        return constructorStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId.value);
    }


    public void insertDriverStartingGrid(RaceId raceId, int position, Driver driver) {
        StartingGridEntity startingGridEntity = new StartingGridEntity(raceId.value, driver.value, position);
        startingGridRepository.save(startingGridEntity);
    }

    public void insertDriverRaceResult(RaceId raceId, String position, Driver driver, Points points, int finishingPosition) {
        RaceResultEntity raceResultEntity = new RaceResultEntity(raceId.value, finishingPosition, position, driver.value, points.value);
        raceResultRepository.save(raceResultEntity);
    }


    public void insertDriverIntoStandings(RaceId raceId, Driver driver, int position, Points points) {
        DriverStandingsEntity driverStandingsEntity = new DriverStandingsEntity(raceId.value, driver.value, position, points.value);
        driverStandingsRepository.save(driverStandingsEntity);
    }


    public void insertConstructorIntoStandings(RaceId raceId, Constructor constructor, int position, Points points) {
        ConstructorStandingsEntity constructorStandingsEntity = new ConstructorStandingsEntity(raceId.value, constructor.value, position, points.value);
        constructorStandingsRepository.save(constructorStandingsEntity);
    }

    public boolean isStartingGridAdded(RaceId raceId) {
        return startingGridRepository.existsByIdRaceId(raceId.value);
    }

    public boolean isRaceResultAdded(RaceId raceId) {
        return raceResultRepository.existsByIdRaceId(raceId.value);
    }

    public RaceId getCurrentRaceIdToGuess() throws NoAvailableRaceException {
        List<StartingGridEntity> startingGridEntities = startingGridRepository.findAllByNotInRaceResult();
        if (startingGridEntities.isEmpty()) {
            throw new NoAvailableRaceException("No starting grid found that is not in race result");
        }
        if (startingGridEntities.size() > 1) {
            throw new NoAvailableRaceException("Too many starting grids found");
        }
        return new RaceId(startingGridEntities.get(0).raceId());
    }

    public List<Driver> getDriverStandings(RaceId raceId, Year year) {
        if (raceId == null) {
            return driverYearRepository.findAllByIdYearOrderByPosition(year).stream()
                    .map(DriverYearEntity::driverName)
                    .map(Driver::new)
                    .toList();
        }
        return driverStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId.value).stream()
                .map(DriverStandingsEntity::driverName)
                .map(Driver::new)
                .toList();

    }

    public List<Constructor> getConstructorStandings(RaceId raceId, Year year) {
        if (raceId == null) {
            return constructorYearRepository.findAllByIdYearOrderByPosition(year).stream()
                    .map(ConstructorYearEntity::constructorName)
                    .map(Constructor::new)
                    .toList();
        }
        return constructorStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId.value).stream()
                .map(ConstructorStandingsEntity::constructorName)
                .map(Constructor::new)
                .toList();
    }

    public List<Driver> getDriversFromStartingGrid(RaceId raceId) {
        return getStartingGrid(raceId).stream()
                .map(StartingGridEntity::driverName)
                .map(Driver::new)
                .toList();
    }

    public List<ColoredCompetitor<Driver>> getDriversFromStartingGridWithColors(RaceId raceId) {
        return startingGridRepository.findAllByRaceIdWithColor(raceId.value).stream()
                .map(ColoredCompetitor::fromIColoredCompetitorToDriver)
                .toList();
    }

}
