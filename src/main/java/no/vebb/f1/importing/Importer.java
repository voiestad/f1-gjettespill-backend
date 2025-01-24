package no.vebb.f1.importing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.TimeUtil;

@Component
public class Importer {

	private static final Logger logger = LoggerFactory.getLogger(Importer.class);

	@Autowired
	private Database db;

	public Importer() {}

	public Importer(Database db) {
		this.db = db;
	}

	@Scheduled(fixedRate = 3600000, initialDelay = 1000)
	public void importData() {
		int year = TimeUtil.getCurrentYear();
		logger.info("Starting import of data to database");
		Map<Integer, List<Integer>> racesToImportFromList = getActiveRaces();

		for (Entry<Integer, List<Integer>> racesToImportFrom : racesToImportFromList.entrySet()) {
			// TODO: Verify line of code
			int raceYear = racesToImportFrom.getKey(); 
			List<Integer> races = racesToImportFrom.getValue();
			importStartingGrids(races);
			importRaceResults(races);
			importSprints(races, raceYear);
		}
		importStandings(year);
		refreshLatestImports(year);
		logger.info("Finished import of data to database");
	}

	private Map<Integer, List<Integer>> getActiveRaces() {
		Map<Integer, List<Integer>> activeRaces = new LinkedHashMap<>();
		List<Map<String, Object>> sqlRes = db.getActiveRaces();
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

	public void importRaceData(int raceId) {
		importStartingGridData(raceId);
		importRaceResultData(raceId);
	}

	private void importStartingGridData(int raceId) {
		List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId);
		if (startingGrid.isEmpty()) {
			return;
		}
		insertStartingGridData(raceId, startingGrid);
	}

	private void importRaceResultData(int raceId) {
		List<List<String>> raceResult = TableImporter.getRaceResult(raceId);
		if (raceResult.isEmpty()) {
			return;
		}
		insertRaceResultData(raceId, raceResult);
	}

	private void refreshLatestImports(int year) {
		refreshLatestStartingGrid(year);
		refreshLatestRaceResult(year);
	}

	private void refreshLatestStartingGrid(int year) {
		try {
			int raceId = db.getLatestStartingGridRaceId(year);
			importStartingGridData(raceId);
		} catch (EmptyResultDataAccessException e) {

		}
	}

	private void refreshLatestRaceResult(int year) {
		try {
			int raceId = db.getLatestRaceResultId(year);
			importRaceResultData(raceId);
		} catch (EmptyResultDataAccessException e) {

		}

	}

	private void importStartingGrids(List<Integer> racesToImportFrom) {
		for (int raceId : racesToImportFrom) {
			boolean isAlreadyAdded = db.isStartingGridAdded(raceId);
			if (isAlreadyAdded) {
				continue;
			}
			List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId);
			if (startingGrid.isEmpty()) {
				break;
			}
			insertStartingGridData(raceId, startingGrid);
		}
	}

	private void insertStartingGridData(int raceId, List<List<String>> startingGrid) {
		for (List<String> row : startingGrid.subList(1, startingGrid.size())) {
			int position = Integer.parseInt(row.get(0));
			String driver = parseDriver(row.get(2));
			db.addDriver(driver);
			db.insertDriverStartingGrid(raceId, position, driver);
		}
	}

	private void importRaceResults(List<Integer> racesToImportFrom) {
		for (int raceId : racesToImportFrom) {
			boolean isAlreadyAdded = db.isRaceResultAdded(raceId);
			if (isAlreadyAdded) {
				throw new RuntimeException("Race is already added and was attempted added again");
			}
			List<List<String>> raceResult = TableImporter.getRaceResult(raceId);
			if (raceResult.isEmpty()) {
				break;
			}
			db.addSprint(raceId);
			insertRaceResultData(raceId, raceResult);
		}
	}

	private void insertRaceResultData(int raceId, List<List<String>> raceResult) {
		int finishingPosition = 1;
		for (List<String> row : raceResult.subList(1, raceResult.size())) {
			String position = row.get(0);
			String driver = parseDriver(row.get(2));
			int points = Integer.parseInt(row.get(6));
			
			db.insertDriverRaceResult(raceId, position, driver, points, finishingPosition);
			finishingPosition++;
		}
	}

	private void importSprints(List<Integer> racesToImportFrom, int year) {
		try {
			int raceNumber = db.getRaceIdForSprint(year);
			if (!racesToImportFrom.contains(raceNumber)) {
				return;
			}
			boolean isAlreadyAdded = db.isSprintAdded(raceNumber);
			if (isAlreadyAdded) {
				return;
			}
			List<List<String>> raceResult = TableImporter.getSprintResult(raceNumber);
			if (raceResult.isEmpty()) {
				return;
			}
			db.addSprint(raceNumber);

		} catch (EmptyResultDataAccessException e) {

		}

	}

	public void importRaceNames(List<Integer> racesToImportFrom, int year) {
		int position = 1;
		for (Integer raceId : racesToImportFrom) {
			if (addRace(raceId, year, position)) {
				position++;
			}
		}
	}

	public void importRaceName(int raceId, int year) {
		int position = db.getMaxRaceOrderPosition(year) + 1;
		addRace(raceId, year, position);
	}

	private boolean addRace(int raceId, int year, int position) {
		boolean isAlreadyAdded = db.isRaceAdded(raceId);
		if (isAlreadyAdded) {
			throw new RuntimeException("Race name was already added");
		}
		String raceName = TableImporter.getGrandPrixName(raceId);
		if (raceName.equals("")) {
			return false;
		}
		db.insertRace(raceId, raceName);
		db.insertRaceOrder(raceId, year, position);
		return true;
	}

	private void importStandings(int year) {
		try {
			int newestRace = db.getMaxRaceId(year);
			importDriverStandings(year, newestRace);
			importConstructorStandings(year, newestRace);
		} catch (EmptyResultDataAccessException e) {
		}
	}

	private void importDriverStandings(int year, int newestRace) {
		List<List<String>> standings = TableImporter.getDriverStandings(year);
		if (standings.size() == 0) {
			return;
		}
		for (List<String> row : standings.subList(1, standings.size())) {
			String driver = parseDriver(row.get(1));
			int position = Integer.parseInt(row.get(0));
			int points = Integer.parseInt(row.get(4));
			db.insertDriverIntoStandings(newestRace, driver, position, points);
		}
	}

	private void importConstructorStandings(int year, int newestRace) {
		List<List<String>> standings = TableImporter.getConstructorStandings(year);
		if (standings.size() == 0) {
			return;
		}
		for (List<String> row : standings.subList(1, standings.size())) {
			String constructor = row.get(1);
			int position = Integer.parseInt(row.get(0));
			int points = Integer.parseInt(row.get(2));
			
			db.addConstructor(constructor);
			db.insertConstructorIntoStandings(newestRace, constructor, position, points);
		}
	}

	private String parseDriver(String driverName) {
		return driverName.substring(0, driverName.length() - 3);
	}
}
