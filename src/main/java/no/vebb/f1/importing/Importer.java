package no.vebb.f1.importing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		List<Map<Integer, Integer>> racesToImportFromList = getActiveRaces();

		for (Map<Integer, Integer> racesToImportFrom : racesToImportFromList) {
			importRaceNames(racesToImportFrom);
			importStartingGrid(racesToImportFrom);
			importRaceResults(racesToImportFrom);
			importSprint(racesToImportFrom);
		}
		importStandings();
		refreshLatestImports();
		logger.info("Finished import of data to database");
	}

	private List<Map<Integer, Integer>> getActiveRaces() {
		List<Map<Integer, Integer>> activeRaces = new ArrayList<>();
		final String sql = "SELECT year, start, end FROM SeasonInfo WHERE active = 1";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql);

		for (Map<String, Object> row : sqlRes) {
			Map<Integer, Integer> season = new LinkedHashMap<>();
			int year = (int) row.get("year");
			int start = (int) row.get("start");
			int end = (int) row.get("end");

			for (int i = start; i <= end; i++) {
				season.put(i, year);
			}
			activeRaces.add(season);
		}

		return activeRaces;
	}

	private void refreshLatestImports() {
		refreshLatestStartingGrid();
		refreshLatestRaceResult();
	}

	private void refreshLatestStartingGrid() {
		final String getStartingGridId = "SELECT MAX(race_number) FROM StartingGrid";
		int raceId = jdbcTemplate.queryForObject(getStartingGridId, Integer.class);
		final String getYear = "SELECT year FROM Race WHERE id = ?";
		int year = jdbcTemplate.queryForObject(getYear, Integer.class, raceId);
		List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId);
		insertStartingGridData(raceId, year, startingGrid);
	}

	private void refreshLatestRaceResult() {
		final String getRaceResultId = "SELECT MAX(race_number) FROM RaceResult";
		int raceId = jdbcTemplate.queryForObject(getRaceResultId, Integer.class);
		List<List<String>> raceResult = TableImporter.getRaceResult(raceId);
		insertRaceResultData(raceId, raceResult);
	}

	private void importStartingGrid(Map<Integer, Integer> racesToImportFrom) {
		final String existCheck = "SELECT COUNT(*) FROM StartingGrid WHERE race_number = ?";
		for (Entry<Integer, Integer> entry : racesToImportFrom.entrySet()) {
			int raceId = entry.getKey();
			int year = entry.getValue();
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

	private void importRaceResults(Map<Integer, Integer> racesToImportFrom) {
		final String existCheck = "SELECT COUNT(*) FROM RaceResult WHERE race_number = ?";
		final String insertSprint = "INSERT OR IGNORE INTO Sprint VALUES (?)";
		for (Entry<Integer, Integer> entry : racesToImportFrom.entrySet()) {
			int raceId = entry.getKey();
			boolean isAlreadyAdded = jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
			if (isAlreadyAdded) {
				continue;
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

	private void importSprint(Map<Integer, Integer> racesToImportFrom) {
		final String existCheck = "SELECT COUNT(*) FROM Sprint WHERE race_number = ?";
		final String insertSprint = "INSERT OR IGNORE INTO Sprint VALUES (?)";
		String sql = "SELECT MAX(race_number) FROM RaceResult";
		Integer maxId = jdbcTemplate.queryForObject(sql, Integer.class);
		int toCheck = maxId + 1;
		if (!racesToImportFrom.containsKey(toCheck)) {
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

	private void importRaceNames(Map<Integer, Integer> racesToImportFrom) {
		final String existCheck = "SELECT COUNT(*) FROM Race WHERE id = ?";
		for (Entry<Integer, Integer> entry : racesToImportFrom.entrySet()) {
			int raceId = entry.getKey();
			int year = entry.getValue();
			boolean isAlreadyAdded = jdbcTemplate.queryForObject(existCheck, Integer.class, raceId) > 0;
			if (isAlreadyAdded) {
				continue;
			}
			String raceName = TableImporter.getGrandPrixName(raceId);
			if (raceName.equals("")) {
				break;
			}
			final String insertRaceName = "INSERT OR IGNORE INTO Race (id, name, year) VALUES (?, ?, ?)";
			jdbcTemplate.update(insertRaceName, raceId, raceName, year);
		}
	}

	private void importStandings() {
		int newestRace = getMaxRaceId();
		final String getYear = "SELECT year FROM Race WHERE id = ?";
		int year = jdbcTemplate.queryForObject(getYear, Integer.class, newestRace);

		importDriverStandings(year, newestRace);
		importConstructorStandings(year, newestRace);
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

	private int getMaxRaceId() {
		String sql = "SELECT MAX(race_number) FROM Sprint";
		Integer maxId = jdbcTemplate.queryForObject(sql, Integer.class);
		return maxId != null ? maxId : -1;
	}

	private String parseDriver(String driverName) {
		return driverName.substring(0, driverName.length() - 3);
	}
}
