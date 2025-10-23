package no.voiestad.f1.results;

import java.util.List;
import java.util.Optional;

import no.voiestad.f1.competitors.constructor.ConstructorEntity;
import no.voiestad.f1.competitors.constructor.ConstructorRepository;
import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.competitors.driver.DriverId;
import no.voiestad.f1.competitors.driver.DriverRepository;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.voiestad.f1.results.constructorStandings.ConstructorStandingsRepository;
import no.voiestad.f1.results.domain.CompetitorPoints;
import no.voiestad.f1.results.domain.CompetitorPosition;
import no.voiestad.f1.results.driverStandings.DriverStandingsEntity;
import no.voiestad.f1.results.driverStandings.DriverStandingsRepository;
import no.voiestad.f1.results.raceResult.RaceResultEntity;
import no.voiestad.f1.results.raceResult.RaceResultRepository;
import no.voiestad.f1.results.startingGrid.StartingGridEntity;
import no.voiestad.f1.results.startingGrid.StartingGridRepository;
import no.voiestad.f1.year.Year;

import org.springframework.stereotype.Service;

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

    public void insertDriverRaceResult(RaceId raceId, String position, DriverEntity driver, CompetitorPoints points, CompetitorPosition finishingPosition) {
        RaceResultEntity raceResultEntity = new RaceResultEntity(raceId, finishingPosition, position, driver, points);
        raceResultRepository.save(raceResultEntity);
    }


    public void insertDriverIntoStandings(RaceId raceId, DriverEntity driver, CompetitorPosition position, CompetitorPoints points) {
        DriverStandingsEntity driverStandingsEntity = new DriverStandingsEntity(raceId, driver, position, points);
        driverStandingsRepository.save(driverStandingsEntity);
    }

    public void insertConstructorIntoStandings(RaceId raceId, ConstructorEntity constructor, CompetitorPosition position, CompetitorPoints points) {
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
