package no.vebb.f1.results;

import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {


    private final StartingGridRepository startingGridRepository;
    private final RaceResultRepository raceResultRepository;
    private final DriverStandingsRepository driverStandingsRepository;
    private final ConstructorStandingsRepository constructorStandingsRepository;

    public ResultService(StartingGridRepository startingGridRepository, RaceResultRepository raceResultRepository, DriverStandingsRepository driverStandingsRepository, ConstructorStandingsRepository constructorStandingsRepository) {
        this.startingGridRepository = startingGridRepository;
        this.raceResultRepository = raceResultRepository;
        this.driverStandingsRepository = driverStandingsRepository;
        this.constructorStandingsRepository = constructorStandingsRepository;
    }

    public List<StartingGridEntity> getStartingGrid(RaceId raceId) {
        return startingGridRepository.findAllByRaceIdOrderByPosition(raceId.value);
    }

    public List<RaceResultEntity> getRaceResult(RaceId raceId) {
        return raceResultRepository.findAllByRaceIdOrderByPosition(raceId.value);
    }

    public List<DriverStandingsEntity> getDriverStandings(RaceId raceId) {
        return driverStandingsRepository.findAllByRaceIdOrderByPosition(raceId.value);
    }

    public List<ConstructorStandingsEntity> getConstructorStandings(RaceId raceId) {
        return constructorStandingsRepository.findAllByRaceIdOrderByPosition(raceId.value);
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
        return startingGridRepository.existsByRaceId(raceId.value);
    }

    public boolean isRaceResultAdded(RaceId raceId) {
        return raceResultRepository.existsByRaceId(raceId.value);
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

}
