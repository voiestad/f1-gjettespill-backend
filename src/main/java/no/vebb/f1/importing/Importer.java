package no.vebb.f1.importing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Importer {

	private static final Logger logger = LoggerFactory.getLogger(Importer.class);
	private final JdbcTemplate jdbcTemplate;

	// TODO: Remove field
	private int year = 2024;

	public Importer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Scheduled(fixedRate = 3600000, initialDelay = 1000)
	public void importData() {
		logger.info("Starting import of data to database");
		Map<Integer, List<Integer>> racesToImportFromList = getActiveRaces();

		for (Entry<Integer, List<Integer>> racesToImportFrom : racesToImportFromList.entrySet()) {
			int year = racesToImportFrom.getKey();
			List<Integer> races = racesToImportFrom.getValue();
			importStartingGrid(races, year);
			importRaceResults(races);
			importSprint(races, year);
		}
		importStandings(year);
		refreshLatestImports(year);
		logger.info("Finished import of data to database");
	}

	private Map<Integer, List<Integer>> getActiveRaces() {
		Map<Integer, List<Integer>> activeRaces = new LinkedHashMap<>();
		final String sql = "SELECT id, year, position FROM Race WHERE id NOT IN (SELECT race_number FROM RaceResult) ORDER BY year ASC, position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> row : sqlRes) {
			int id = (int) row.get("id");
			int year = (int) row.get("year");
			if (!activeRaces.containsKey(year)) {
				activeRaces.put(year, new ArrayList<>());
			}
			List<Integer> races = activeRaces.get(year);
			races.add(id);
		}

		return activeRaces;
	}

	private void refreshLatestImports(int year) {
		refreshLatestStartingGrid(year);
		refreshLatestRaceResult(year);
	}

	private void refreshLatestStartingGrid(int year) {
		try {
			final String getStartingGridId = """
					SELECT DISTINCT r.id
					FROM StartingGrid sg
					JOIN Race r ON r.id = sg.race_number
					WHERE r.position = (
						SELECT MAX(r2.position)
						FROM Race r2
						WHERE r2.year = ?
					)
					AND r.year = ?;
					""";
			Integer raceId = jdbcTemplate.queryForObject(getStartingGridId, Integer.class, year, year);
			if (raceId == null) {
				return;
			}
			List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId);
			insertStartingGridData(raceId, year, startingGrid);
		} catch (EmptyResultDataAccessException e) {

		}
	}

	private void refreshLatestRaceResult(int year) {
		try {
			final String getRaceResultId = """
					SELECT DISTINCT r.id
					FROM RaceResult rr
					JOIN Race r on r.id = rr.race_number
					WHERE r.position = (
							SELECT MAX(r2.position)
							FROM Race r2
							WHERE r2.year = ?
						)
					AND r.year = ?;
					""";
			Integer raceId = jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year, year);
			if (raceId == null) {
				return;
			}
			List<List<String>> raceResult = TableImporter.getRaceResult(raceId);
			insertRaceResultData(raceId, raceResult);
		} catch (EmptyResultDataAccessException e) {

		}

	}

	private void importStartingGrid(List<Integer> racesToImportFrom, int year) {
		final String existCheck = "SELECT COUNT(*) FROM StartingGrid WHERE race_number = ?";
		for (int raceId : racesToImportFrom) {
			boolean isAlreadyAdded = jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
			if (isAlreadyAdded) {
				continue;
			}
			List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId);
			if (startingGrid.isEmpty()) {
				break;
			}
			insertStartingGridData(raceId, year, startingGrid);
		}
	}

	private void insertStartingGridData(int raceId, int year, List<List<String>> startingGrid) {
		final String insertDriver = "INSERT OR IGNORE INTO Driver (name) VALUES (?)";
		final String insertDriverYear = "INSERT OR IGNORE INTO DriverYear (driver, year) VALUES (?, ?)";
		final String insertStartingGrid = "INSERT OR REPLACE INTO StartingGrid (race_number, position, driver) VALUES (?, ?, ?)";
		for (List<String> row : startingGrid.subList(1, startingGrid.size())) {
			String position = row.get(0);
			String driver = parseDriver(row.get(2));
			jdbcTemplate.update(insertDriver, driver);
			jdbcTemplate.update(insertDriverYear, driver, year);
			jdbcTemplate.update(insertStartingGrid, raceId, position, driver);
		}
	}

	private void importRaceResults(List<Integer> racesToImportFrom) {
		final String existCheck = "SELECT COUNT(*) FROM RaceResult WHERE race_number = ?";
		final String insertSprint = "INSERT OR IGNORE INTO Sprint VALUES (?)";
		for (int raceId : racesToImportFrom) {
			boolean isAlreadyAdded = jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
			if (isAlreadyAdded) {
				throw new RuntimeException("Race is already added and was attempted added again");
			}
			List<List<String>> raceResult = TableImporter.getRaceResult(raceId);
			if (raceResult.isEmpty()) {
				break;
			}
			jdbcTemplate.update(insertSprint, raceId);
			insertRaceResultData(raceId, raceResult);
		}
	}

	private void insertRaceResultData(int raceId, List<List<String>> raceResult) {
		final String insertRaceResult = "INSERT OR REPLACE INTO RaceResult (race_number, position, driver, points, finishing_position) VALUES (?, ?, ?, ?, ?)";
		int finishingPosition = 1;
		for (List<String> row : raceResult.subList(1, raceResult.size())) {
			String position = row.get(0);
			String driver = parseDriver(row.get(2));
			String points = row.get(6);

			jdbcTemplate.update(insertRaceResult, raceId, position, driver, points, finishingPosition);
			finishingPosition++;
		}
	}

	private void importSprint(List<Integer> racesToImportFrom, int year) {
		final String existCheck = "SELECT COUNT(*) FROM Sprint WHERE race_number = ?";
		final String insertSprint = "INSERT OR IGNORE INTO Sprint VALUES (?)";
		final String getRaceResultId = """
				SELECT DISTINCT r.id
				FROM RaceResult rr
				JOIN Race r on r.id = rr.race_number
				WHERE r.position = (
						SELECT MIN(r2.position)
						FROM Race r2
						WHERE r2.year = ?
					)
				AND r.year = ?
				AND r.id NOT IN (SELECT race_number FROM RaceResult);
				""";
		Integer toCheck = jdbcTemplate.queryForObject(getRaceResultId, Integer.class, year, year);
		if (!racesToImportFrom.contains(toCheck)) {
			return;
		}
		boolean isAlreadyAdded = jdbcTemplate.queryForObject(existCheck, Integer.class, toCheck) > 0;
		if (isAlreadyAdded) {
			return;
		}
		List<List<String>> raceResult = TableImporter.getSprintResult(toCheck);
		if (raceResult.isEmpty()) {
			return;
		}
		jdbcTemplate.update(insertSprint, toCheck);
	}

	public void importRaceNames(List<Integer> racesToImportFrom, int year) {
		int position = 1;
		for (Integer raceId : racesToImportFrom) {
			if(addRace(raceId, year, position)) {
				position++;
			}
		}
	}

	public void importRaceName(int raceId, int year) {
		final String positionFinder = "SELECT MAX(position) FROM Race WHERE year = ?";
		int position = jdbcTemplate.queryForObject(positionFinder, Integer.class, year) + 1;
		addRace(raceId, year, position);
	}

	private boolean addRace(int raceId, int year, int position) {
		final String existCheck = "SELECT COUNT(*) FROM Race WHERE id = ?";
		boolean isAlreadyAdded = jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
		if (isAlreadyAdded) {
			throw new RuntimeException("Race name was already added");
		}
		String raceName = TableImporter.getGrandPrixName(raceId);
		if (raceName.equals("")) {
			return false;
		}
		final String insertRaceName = "INSERT OR IGNORE INTO Race (id, name, year, position) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(insertRaceName, raceId, raceName, year, position);
		return true;
	}

	private void importStandings(int year) {
		try {
			int newestRace = getMaxRaceId(year);
			importDriverStandings(year, newestRace);
			importConstructorStandings(year, newestRace);

		} catch (EmptyResultDataAccessException e) {
		}
	}

	private void importDriverStandings(int year, int newestRace) {
		List<List<String>> standings = TableImporter.getDriverStandings(year);
		final String insertDriverStandings = "INSERT OR REPLACE INTO DriverStandings (race_number, driver, position, points) VALUES (?, ?, ?, ?)";
		for (List<String> row : standings.subList(1, standings.size())) {
			String driver = parseDriver(row.get(1));
			int position = Integer.parseInt(row.get(0));
			String points = row.get(4);
			jdbcTemplate.update(insertDriverStandings, newestRace, driver, position, points);
		}
	}

	private void importConstructorStandings(int year, int newestRace) {
		List<List<String>> standings = TableImporter.getConstructorStandings(year);
		final String insertConstructor = "INSERT OR IGNORE INTO Constructor (name) VALUES (?)";
		final String insertConstructorYear = "INSERT OR IGNORE INTO ConstructorYear (constructor, year) VALUES (?, ?)";
		final String insertConstructorStandings = "INSERT OR REPLACE INTO ConstructorStandings (race_number, constructor, position, points) VALUES (?, ?, ?, ?)";
		for (List<String> row : standings.subList(1, standings.size())) {
			String constructor = row.get(1);
			int position = Integer.parseInt(row.get(0));
			String points = row.get(2);

			jdbcTemplate.update(insertConstructor, constructor);
			jdbcTemplate.update(insertConstructorYear, constructor, year);
			jdbcTemplate.update(insertConstructorStandings, newestRace, constructor, position, points);
		}
	}

	private int getMaxRaceId(int year) {
		final String sql = """
				SELECT DISTINCT r.id
				FROM Sprint s
				JOIN Race r ON r.id = s.race_number
				WHERE r.position = (
				    SELECT MAX(r2.position)
				    FROM Race r2
				    WHERE r2.year = ?
				)
				AND r.year = ?;
				""";

		Integer maxId = jdbcTemplate.queryForObject(sql, Integer.class, year, year);
		return maxId != null ? maxId : -1;
	}

	private String parseDriver(String driverName) {
		return driverName.substring(0, driverName.length() - 3);
	}
}
