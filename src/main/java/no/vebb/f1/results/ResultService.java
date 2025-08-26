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

    public List<StartingGrid> getStartingGrid(RaceId raceId) {
        return startingGridRepository.findAllByRaceIdOrderByPosition(raceId.value);
    }

    public List<RaceResult> getRaceResult(RaceId raceId) {
        return raceResultRepository.findAllByRaceIdOrderByPosition(raceId.value);
    }

    public List<DriverStandings> getDriverStandings(RaceId raceId) {
        return driverStandingsRepository.findAllByRaceIdOrderByPosition(raceId.value);
    }

    public List<ConstructorStandings> getConstructorStandings(RaceId raceId) {
        return constructorStandingsRepository.findAllByRaceIdOrderByPosition(raceId.value);
    }


    public void insertDriverStartingGrid(RaceId raceId, int position, Driver driver) {
        StartingGrid startingGrid = new StartingGrid(raceId.value, driver.value, position);
        startingGridRepository.save(startingGrid);
    }

    public void insertDriverRaceResult(RaceId raceId, String position, Driver driver, Points points, int finishingPosition) {
        RaceResult raceResult = new RaceResult(raceId.value, finishingPosition, position, driver.value, points.value);
        raceResultRepository.save(raceResult);
    }


    public void insertDriverIntoStandings(RaceId raceId, Driver driver, int position, Points points) {
        DriverStandings driverStandings = new DriverStandings(raceId.value, driver.value, position, points.value);
        driverStandingsRepository.save(driverStandings);
    }


    public void insertConstructorIntoStandings(RaceId raceId, Constructor constructor, int position, Points points) {
        ConstructorStandings constructorStandings = new ConstructorStandings(raceId.value, constructor.value, position, points.value);
        constructorStandingsRepository.save(constructorStandings);
    }

    public boolean isStartingGridAdded(RaceId raceId) {
        return startingGridRepository.existsByRaceId(raceId.value);
    }

    public boolean isRaceResultAdded(RaceId raceId) {
        return raceResultRepository.existsByRaceId(raceId.value);
    }

    public RaceId getCurrentRaceIdToGuess() throws NoAvailableRaceException {
        List<StartingGrid> startingGrids = startingGridRepository.findAllByNotInRaceResult();
        if (startingGrids.isEmpty()) {
            throw new NoAvailableRaceException("No starting grid found that is not in race result");
        }
        if (startingGrids.size() > 1) {
            throw new NoAvailableRaceException("Too many starting grids found");
        }
        return new RaceId(startingGrids.get(0).raceId());
    }

}
