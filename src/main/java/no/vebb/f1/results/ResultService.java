package no.vebb.f1.results;

import no.vebb.f1.util.collection.ColoredCompetitor;
import no.vebb.f1.util.domainPrimitive.*;
import no.vebb.f1.util.exception.NoAvailableRaceException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {


    private final StartingGridRepository startingGridRepository;
    private final RaceResultRepository raceResultRepository;
    private final DriverStandingsRepository driverStandingsRepository;
    private final ConstructorStandingsRepository constructorStandingsRepository;
    private final JdbcTemplate jdbcTemplate;

    public ResultService(StartingGridRepository startingGridRepository, RaceResultRepository raceResultRepository, DriverStandingsRepository driverStandingsRepository, ConstructorStandingsRepository constructorStandingsRepository, JdbcTemplate jdbcTemplate) {
        this.startingGridRepository = startingGridRepository;
        this.raceResultRepository = raceResultRepository;
        this.driverStandingsRepository = driverStandingsRepository;
        this.constructorStandingsRepository = constructorStandingsRepository;
        this.jdbcTemplate = jdbcTemplate;
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
        final String driverYearSql = "SELECT driver_name FROM drivers_year WHERE year = ? ORDER BY position;";
        final String driverStandingsSql = "SELECT driver_name FROM driver_standings WHERE race_id = ? ORDER BY position;";
        List<String> result = getCompetitors(raceId, year, driverYearSql, driverStandingsSql);
        return result.stream().map(Driver::new).toList();
    }

    private List<String> getCompetitors(RaceId raceId, Year year, final String competitorYearSql, final String competitorStandingsSql) {
        List<String> result;
        if (raceId == null) {
            result = jdbcTemplate.queryForList(competitorYearSql, String.class, year.value);
        } else {
            result = jdbcTemplate.queryForList(competitorStandingsSql, String.class, raceId.value);
        }
        return result;
    }

    public List<Constructor> getConstructorStandings(RaceId raceId, Year year) {
        final String constructorYearSql = "SELECT constructor_name FROM constructors_year WHERE year = ? ORDER BY position;";
        final String constructorStandingsSql = """
                SELECT constructor_name
                FROM constructor_standings
                WHERE race_id = ?
                ORDER BY position;
                """;
        List<String> result = getCompetitors(raceId, year, constructorYearSql, constructorStandingsSql);
        return result.stream().map(Constructor::new).toList();
    }

    public List<Driver> getDriversFromStartingGrid(RaceId raceId) {
        final String getDriversFromGrid = "SELECT driver_name FROM starting_grids WHERE race_id = ? ORDER BY position;";
        return jdbcTemplate.queryForList(getDriversFromGrid, String.class, raceId.value).stream()
                .map(Driver::new)
                .toList();
    }

    public List<ColoredCompetitor<Driver>> getDriversFromStartingGridWithColors(RaceId raceId) {
        final String getDriversFromGrid = """
                SELECT sg.driver_name as driver, cc.color as color
                FROM starting_grids sg
                LEFT JOIN drivers_team dt ON dt.driver_name = sg.driver_name
                LEFT JOIN constructors_color cc ON cc.constructor_name = dt.team
                WHERE race_id = ?
                ORDER BY position;
                """;
        return jdbcTemplate.queryForList(getDriversFromGrid, raceId.value).stream()
                .map(row ->
                        new ColoredCompetitor<>(
                                new Driver((String) row.get("driver")),
                                new Color((String) row.get("color")))
                )
                .toList();
    }

}
