package no.vebb.f1.importing;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Importer {
	
	private static final Logger logger = LoggerFactory.getLogger(Importer.class);
	private final JdbcTemplate jdbcTemplate;

	public Importer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Scheduled(fixedRate = 3600000, initialDelay = 1000)
	public void importData() {
		logger.info("Starting import of data to database");
		int newestRace = getMaxRaceId();
		importRaceNames(newestRace);
		int oldNewestRace = newestRace;
		newestRace = getMaxRaceId();
		importDriverStandings(newestRace);
		importConstructorStandings(newestRace);
		importStartingGrid(oldNewestRace);
		importRaceResults(oldNewestRace);
		importSprintResults(oldNewestRace, newestRace);
		logger.info("Finished import of data to database");
	}

	private void importStartingGrid(int raceNumber) {
		final String insertStartingGrid = "INSERT OR IGNORE INTO StartingGrid (race_number, position, driver) VALUES (?, ?, ?)";
		while (true) {
			List<List<String>> startingGrid = TableImporter.getStartingGrid(raceNumber);
			if (startingGrid.isEmpty()) {
				break;
			}
			for (List<String> row : startingGrid.subList(1, startingGrid.size())) {
				String position = row.get(0);
				String driver = parseDriver(row.get(2));
				jdbcTemplate.update(insertStartingGrid, raceNumber, position, driver);
			}
			raceNumber++;
		}
	}

	private void importRaceResults(int raceNumber) {
		final String insertRaceResult = "INSERT OR IGNORE INTO RaceResult (race_number, position, driver, points, finishing_position) VALUES (?, ?, ?, ?, ?)";
		while (true) {
			List<List<String>> raceResult = TableImporter.getRaceResult(raceNumber);
			if (raceResult.isEmpty()) {
				break;
			}
			int finishingPosition = 1;
			for (List<String> row : raceResult.subList(1, raceResult.size())) {
				String position = row.get(0);
				String driver = parseDriver(row.get(2));
				String points = row.get(6);

				jdbcTemplate.update(insertRaceResult, raceNumber, position, driver, points, finishingPosition);
				finishingPosition++;
			}
			raceNumber++;
		}
	}

	private void importSprintResults(int raceNumberFrom, int raceNumberTo) {
		int raceNumber = raceNumberFrom;
		final String insertSprintResult = "INSERT OR IGNORE INTO SprintResult (race_number, position, driver, points, finishing_position) VALUES (?, ?, ?, ?, ?)";
		while (raceNumber < raceNumberTo) {
			List<List<String>> sprintResult = TableImporter.getSprintResult(raceNumber);
			if (sprintResult.isEmpty()) {
				raceNumber++;
				continue;
			}
			int finishingPosition = 1;
			for (List<String> row : sprintResult.subList(1, sprintResult.size())) {
				String position = row.get(0);
				String driver = parseDriver(row.get(2));
				String points = row.get(6);

				jdbcTemplate.update(insertSprintResult, raceNumber, position, driver, points, finishingPosition);
				finishingPosition++;
			}
			raceNumber++;
		}
	}

	private void importRaceNames(int raceNumber) {
		while (true) {
			String raceName = TableImporter.getGrandPrixName(raceNumber);
			if (raceName.equals("")) {
				break;
			}
			final String insertRaceName = "INSERT OR IGNORE INTO Race (id, name) VALUES (?, ?)";
			jdbcTemplate.update(insertRaceName, raceNumber, raceName);
			raceNumber++;
		}
	}

	private void importDriverStandings(int raceNumber) {
		int year = 2024;
		List<List<String>> standings = TableImporter.getDriverStandings(year);
		final String insertDriver = "INSERT OR IGNORE INTO Driver (name) VALUES (?)";
		final String insertDriverYear = "INSERT OR IGNORE INTO DriverYear (driver, year) VALUES (?, ?)";
		final String insertDriverStandings = "INSERT OR IGNORE INTO DriverStandings (race_number, driver, position, points) VALUES (?, ?, ?, ?)";
		for (List<String> row : standings.subList(1, standings.size())) {
			String driver = parseDriver(row.get(1));
			int position = Integer.parseInt(row.get(0));
			String points = row.get(4);

			jdbcTemplate.update(insertDriver, driver);
			jdbcTemplate.update(insertDriverYear, driver, year);
			jdbcTemplate.update(insertDriverStandings, raceNumber, driver, position, points);
		}
	}

	private void importConstructorStandings(int raceNumber) {
		int year = 2024;
		List<List<String>> standings = TableImporter.getConstructorStandings(year);
		final String insertConstructor = "INSERT OR IGNORE INTO Constructor (name) VALUES (?)";
		final String insertConstructorYear = "INSERT OR IGNORE INTO ConstructorYear (constructor, year) VALUES (?, ?)";
		final String insertConstructorStandings = "INSERT OR IGNORE INTO ConstructorStandings (race_number, constructor, position, points) VALUES (?, ?, ?, ?)";
		for (List<String> row : standings.subList(1, standings.size())) {
			String constructor = row.get(1);
			int position = Integer.parseInt(row.get(0));
			String points = row.get(2);

			jdbcTemplate.update(insertConstructor, constructor);
			jdbcTemplate.update(insertConstructorYear, constructor, year);
			jdbcTemplate.update(insertConstructorStandings, raceNumber, constructor, position, points);
		}
	}

	private int getMaxRaceId() {
        String sql = "SELECT MAX(id) FROM Race";
        Integer maxId = jdbcTemplate.queryForObject(sql, Integer.class);
        return maxId != null ? maxId : 1252;
    }

	private String parseDriver(String driverName) {
		return driverName.substring(0, driverName.length() - 3);
	}
}
