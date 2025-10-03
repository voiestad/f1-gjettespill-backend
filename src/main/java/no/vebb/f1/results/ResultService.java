package no.vebb.f1.results;

import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.constructor.ConstructorId;
import no.vebb.f1.competitors.constructor.ConstructorRepository;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.competitors.driver.DriverId;
import no.vebb.f1.competitors.driver.DriverRepository;
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
import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResultService {

    private final StartingGridRepository startingGridRepository;
    private final RaceResultRepository raceResultRepository;
    private final DriverStandingsRepository driverStandingsRepository;
    private final ConstructorStandingsRepository constructorStandingsRepository;
    private final DriverRepository driverRepository;
    private final ConstructorRepository constructorRepository;

    public ResultService(StartingGridRepository startingGridRepository, RaceResultRepository raceResultRepository, DriverStandingsRepository driverStandingsRepository, ConstructorStandingsRepository constructorStandingsRepository, DriverRepository driverRepository, ConstructorRepository constructorRepository) {
        this.startingGridRepository = startingGridRepository;
        this.raceResultRepository = raceResultRepository;
        this.driverStandingsRepository = driverStandingsRepository;
        this.constructorStandingsRepository = constructorStandingsRepository;
        this.driverRepository = driverRepository;
        this.constructorRepository = constructorRepository;
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


    public void insertDriverStartingGrid(RaceId raceId, CompetitorPosition position, DriverEntity driver) {
        StartingGridEntity startingGridEntity = new StartingGridEntity(raceId, driver, position);
        startingGridRepository.save(startingGridEntity);
    }

    public void insertDriverRaceResult(RaceId raceId, String position, DriverId driverId, CompetitorPoints points, CompetitorPosition finishingPosition) {
        RaceResultEntity raceResultEntity = new RaceResultEntity(raceId, finishingPosition, position, driverId, points);
        raceResultRepository.save(raceResultEntity);
    }


    public void insertDriverIntoStandings(RaceId raceId, DriverId driverId, CompetitorPosition position, CompetitorPoints points) {
        DriverStandingsEntity driverStandingsEntity = new DriverStandingsEntity(raceId, driverId, position, points);
        driverStandingsRepository.save(driverStandingsEntity);
    }


    public void insertConstructorIntoStandings(RaceId raceId, ConstructorId constructor, CompetitorPosition position, CompetitorPoints points) {
        ConstructorStandingsEntity constructorStandingsEntity = new ConstructorStandingsEntity(raceId, constructor, position, points);
        constructorStandingsRepository.save(constructorStandingsEntity);
    }

    public boolean isStartingGridAdded(RaceId raceId) {
        return startingGridRepository.existsByIdRaceId(raceId);
    }

    public boolean isRaceResultAdded(RaceId raceId) {
        return raceResultRepository.existsByIdRaceId(raceId);
    }

    public Optional<RaceId> getCurrentRaceIdToGuess() {
        List<StartingGridEntity> startingGridEntities = startingGridRepository.findAllByNotInRaceResult();
        if (startingGridEntities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(startingGridEntities.get(0).raceId());
    }

    public List<DriverEntity> getDriverStandings(RaceId raceId, Year year) {
        if (raceId == null) {
            return driverRepository.findAllByYearOrderByPosition(year);
        }
        return driverStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId).stream()
                .map(DriverStandingsEntity::driver)
                .toList();

    }

    public List<ConstructorEntity> getConstructorStandings(RaceId raceId, Year year) {
        if (raceId == null) {
            return constructorRepository.findAllByYearOrderByPosition(year);
        }
        return constructorStandingsRepository.findAllByIdRaceIdOrderByPosition(raceId).stream()
                .map(ConstructorStandingsEntity::constructor)
                .toList();
    }

    public List<DriverId> getDriversFromStartingGrid(RaceId raceId) {
        return getStartingGrid(raceId).stream()
                .map(StartingGridEntity::driver)
                .map(DriverEntity::driverId)
                .toList();
    }

}
