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
import no.vebb.f1.util.collection.CutoffRace;
import no.vebb.f1.util.collection.PositionedCompetitor;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

@Component
public class Importer {

	private static final Logger logger = LoggerFactory.getLogger(Importer.class);

	@Autowired
	private Database db;

	public Importer() {}

	public Importer(Database db) {
		this.db = db;
	}

	@Scheduled(fixedRate = 3600000, initialDelay = 5000)
	public void importData() {
		logger.info("Starting import of data to database");
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			Map<Year, List<RaceId>> racesToImportFromList = getActiveRaces();
			boolean shouldImportStandings = false;

			for (Entry<Year, List<RaceId>> racesToImportFrom : racesToImportFromList.entrySet()) {
				Year raceYear = racesToImportFrom.getKey(); 
				List<RaceId> races = racesToImportFrom.getValue();
				importStartingGrids(races);
				
				if ((importRaceResults(races) || !areStandingsUpToDate(year))
						&& year.equals(raceYear)) {
					shouldImportStandings = true;
				}
				importSprints(races, raceYear);
			}
			if (refreshLatestImports(year)) {
				shouldImportStandings = true;
			}
			if (shouldImportStandings) {
				importStandings(year);
			}
			logger.info("Finished import of data to database");
		} catch (InvalidYearException e) {
			logger.error("Could not import data to the database because current year is not valid");
		}
	}

	private boolean areStandingsUpToDate(Year year) {
		try {
			RaceId latestRaceId = db.getLatestRaceResultId(year);
			RaceId standingsRaceId = db.getLatestStandingsId(year);
			return latestRaceId.equals(standingsRaceId);
		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	private Map<Year, List<RaceId>> getActiveRaces() {
		Map<Year, List<RaceId>> activeRaces = new LinkedHashMap<>();
		List<CutoffRace> sqlRes = db.getActiveRaces();
		for (CutoffRace race : sqlRes) {
			if (!activeRaces.containsKey(race.year)) {
				activeRaces.put(race.year, new ArrayList<>());
			}
			List<RaceId> races = activeRaces.get(race.year);
			races.add(race.id);
		}
		return activeRaces;
	}

	public void importRaceData(RaceId raceId) {
		importStartingGridData(raceId);
		importRaceResultData(raceId);
	}

	private void importStartingGridData(RaceId raceId) {
		List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId.value);
		if (startingGrid.isEmpty()) {
			return;
		}
		insertStartingGridData(raceId, startingGrid);
	}

	private boolean importRaceResultData(RaceId raceId) {
		List<List<String>> raceResult = TableImporter.getRaceResult(raceId.value);
		if (raceResult.isEmpty()) {
			return false;
		}
		List<PositionedCompetitor> preList = db.getRaceResult(raceId);
		insertRaceResultData(raceId, raceResult);
		List<PositionedCompetitor> postList = db.getRaceResult(raceId);
		if (preList.size() != postList.size()) {
			return true;
		}
		for (int i = 0; i < preList.size(); i++) {
			PositionedCompetitor pre = preList.get(i);
			PositionedCompetitor post = postList.get(i);
			if (!pre.equals(post)) {
				return true;
			}
		}
		return false;
	}

	private boolean refreshLatestImports(Year year) {
		refreshLatestStartingGrid(year);
		return refreshLatestRaceResult(year);
	}

	private void refreshLatestStartingGrid(Year year) {
		try {
			RaceId raceId = db.getLatestStartingGridRaceId(year);
			importStartingGridData(raceId);
		} catch (EmptyResultDataAccessException e) {

		}
	}

	private boolean refreshLatestRaceResult(Year year) {
		try {
			RaceId raceId = db.getLatestRaceResultId(year);
			return importRaceResultData(raceId);
		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	private void importStartingGrids(List<RaceId> racesToImportFrom) {
		for (RaceId raceId : racesToImportFrom) {
			boolean isAlreadyAdded = db.isStartingGridAdded(raceId);
			if (isAlreadyAdded) {
				continue;
			}
			List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId.value);
			if (startingGrid.isEmpty()) {
				break;
			}
			insertStartingGridData(raceId, startingGrid);
		}
	}

	private void insertStartingGridData(RaceId raceId, List<List<String>> startingGrid) {
		for (List<String> row : startingGrid.subList(1, startingGrid.size())) {
			int position = Integer.parseInt(row.get(0));
			String driver = parseDriver(row.get(2));
			db.addDriver(driver);
			Driver validDriver = new Driver(driver, db);
			db.insertDriverStartingGrid(raceId, position, validDriver);
		}
	}

	private boolean importRaceResults(List<RaceId> racesToImportFrom) {
		boolean addedNewRace = false;
		for (RaceId raceId : racesToImportFrom) {
			boolean isAlreadyAdded = db.isRaceResultAdded(raceId);
			if (isAlreadyAdded) {
				throw new RuntimeException("Race is already added and was attempted added again");
			}
			List<List<String>> raceResult = TableImporter.getRaceResult(raceId.value);
			if (raceResult.isEmpty()) {
				break;
			}
			db.addSprint(raceId);
			insertRaceResultData(raceId, raceResult);
			addedNewRace = true;
		}
		return addedNewRace;
	}

	private void insertRaceResultData(RaceId raceId, List<List<String>> raceResult) {
		int finishingPosition = 1;
		for (List<String> row : raceResult.subList(1, raceResult.size())) {
			String position = row.get(0);
			Driver driver = new Driver(parseDriver(row.get(2)), db);

			Points points = new Points((int) Double.parseDouble(row.get(6)));
			
			db.insertDriverRaceResult(raceId, position, driver, points, finishingPosition);
			finishingPosition++;
		}
	}

	private void importSprints(List<RaceId> racesToImportFrom, Year year) {
		try {
			RaceId raceId = db.getRaceIdForSprint(year);
			if (!racesToImportFrom.contains(raceId)) {
				return;
			}
			boolean isAlreadyAdded = db.isSprintAdded(raceId);
			if (isAlreadyAdded) {
				return;
			}
			List<List<String>> raceResult = TableImporter.getSprintResult(raceId.value);
			if (raceResult.isEmpty()) {
				return;
			}
			db.addSprint(raceId);

		} catch (EmptyResultDataAccessException e) {

		}

	}

	public void importRaceNames(List<Integer> racesToImportFrom, int year) {
		int position = 1;
		for (int raceId : racesToImportFrom) {
			if (addRace(raceId, year, position)) {
				position++;
			}
		}
	}

	public void importRaceName(int raceId, Year year) {
		int position = db.getMaxRaceOrderPosition(year) + 1;
		addRace(raceId, year.value, position);
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
		RaceId validRaceId = new RaceId(raceId, db);
		db.insertRaceOrder(validRaceId, year, position);
		return true;
	}

	private void importStandings(Year year) {
		try {
			RaceId newestRace = db.getLatestRaceId(year);
			importDriverStandings(year, newestRace);
			importConstructorStandings(year, newestRace);
		} catch (EmptyResultDataAccessException e) {
		} catch (InvalidYearException e) {
		}
	}

	private void importDriverStandings(Year year, RaceId newestRace) {
		List<List<String>> standings = TableImporter.getDriverStandings(year.value);
		if (standings.size() == 0) {
			return;
		}
		standings = standings.subList(1, standings.size());
		List<PositionedCompetitor> currentStandings = standings.stream()
			.map(row -> 
			new PositionedCompetitor(
				String.valueOf(Integer.parseInt(row.get(0))),
				parseDriver(row.get(1)), 
				String.valueOf((int) Double.parseDouble(row.get(4)))
				))
			.toList();
		if (!isDriverStandingsNew(currentStandings, year)) {
			return;
		}
		for (PositionedCompetitor competitor : currentStandings) {
			Driver driver = new Driver(competitor.name, db);
			int position = Integer.parseInt(competitor.position);
			Points points = new Points(Integer.parseInt(competitor.points));
			db.insertDriverIntoStandings(newestRace, driver, position, points);
		}
	}

	private boolean isDriverStandingsNew(List<PositionedCompetitor> standings, Year year) {
		try {
			RaceId previousRaceId = db.getLatestStandingsId(year);
			List<PositionedCompetitor> previousStandings = db.getDriverStandings(previousRaceId);
			return compareStandings(standings, previousStandings);
		} catch (EmptyResultDataAccessException e) {
			return true;
		}
	}

	private void importConstructorStandings(Year year, RaceId newestRace) {
		List<List<String>> standings = TableImporter.getConstructorStandings(year.value);
		if (standings.size() == 0) {
			return;
		}
		standings = standings.subList(1, standings.size());
		List<PositionedCompetitor> currentStandings = standings.stream()
			.map(row -> 
			new PositionedCompetitor(
				String.valueOf(Integer.parseInt(row.get(0))),
				row.get(1),
				String.valueOf((int) Double.parseDouble(row.get(2)))
				))
			.toList();
		if (!isConstructorStandingsNew(currentStandings, year)) {
			return;
		}
		for (PositionedCompetitor competitor : currentStandings) {
			int position = Integer.parseInt(competitor.position);
			Points points = new Points(Integer.parseInt(competitor.points));
			Constructor validConstructor = new Constructor(competitor.name);
			db.insertConstructorIntoStandings(newestRace, validConstructor, position, points);
		}
	}

	private boolean isConstructorStandingsNew(List<PositionedCompetitor> standings, Year year) {
		try {
			RaceId previousRaceId = db.getLatestStandingsId(year);
			List<PositionedCompetitor> previousStandings = db.getConstructorStandings(previousRaceId);
			return compareStandings(standings, previousStandings);
		} catch (EmptyResultDataAccessException e) {
			return true;
		}
	}

	private boolean compareStandings(List<PositionedCompetitor> standings, List<PositionedCompetitor> previousStandings) {
		if (standings.size() != previousStandings.size()) {
			return true;
		}
		for (int i = 0; i < standings.size(); i++) {
			PositionedCompetitor previousCompetitor = previousStandings.get(i);
			PositionedCompetitor competitor = standings.get(i);
			if (!previousCompetitor.equals(competitor)) {
				return true;
			}
		}
		return false;
	}

	private String parseDriver(String driverName) {
		return driverName.substring(0, driverName.length() - 3);
	}
}
