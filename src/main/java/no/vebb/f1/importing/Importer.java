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
import org.springframework.transaction.annotation.Transactional;

import no.vebb.f1.database.Database;
import no.vebb.f1.graph.GraphCache;
import no.vebb.f1.user.UserMailService;
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

	@Autowired
	private UserMailService userMailService;

	@Autowired
	private GraphCache graphCache;

	public Importer() {}

	public Importer(Database db) {
		this.db = db;
	}

	@Scheduled(fixedDelay = TimeUtil.TEN_MINUTES, initialDelay = TimeUtil.SECOND * 5)
	@Transactional
	public void importData() {
		logger.info("Starting import of data to database");
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			Map<Year, List<RaceId>> racesToImportFromList = getActiveRaces();
			boolean shouldImportStandings = false;

			for (Entry<Year, List<RaceId>> racesToImportFrom : racesToImportFromList.entrySet()) {
				Year raceYear = racesToImportFrom.getKey(); 
				List<RaceId> races = racesToImportFrom.getValue();
				logger.info("Year '{}' has '{}' races to import", raceYear, races.size());
				int startingGridCount = importStartingGrids(races);
				logger.info("Imported '{}' starting grid", startingGridCount);
				boolean hasAddedNewRaceResult = importRaceResults(races);
				if (year.equals(raceYear)) {
					if (hasAddedNewRaceResult) {
						logger.info("New race result imported, will import standings");
						shouldImportStandings = true;
					} else if (!areStandingsUpToDate(year)) {
						logger.info("Standings not up to date, will import standings");
						shouldImportStandings = true;
					} else {
						logger.info("Standings and race results are up to date");
					}
				}
			}
			ResultChangeStatus changeStatus = refreshLatestImports(year);
			if (!changeStatus.equals(ResultChangeStatus.NO_CHANGE)) {
				logger.info("Changes to the race result, will import standings");
				shouldImportStandings = true;
			}
			if (shouldImportStandings) {
				boolean standingsNew = importStandings(year, changeStatus.getPointsChange());
				if (!standingsNew) {
					if (!changeStatus.equals(ResultChangeStatus.OUTSIDE_POINTS_CHANGE)) {
						logger.error("Standings were not new. Rolling back.");
						throw new RuntimeException("Standings were not up to date with race result.");	
					} else {
						graphCache.refresh();
						logger.info("Race result changed outside points without standings changing. Sending message to admins.");
						userMailService.sendServerMessageToAdmins(
							"Endringer i resultat av løp utenfor poengene uten at mesterskapet endret seg. Vennligst verifiser at mesterskapet er korrekt.");
					}
				} else {
					graphCache.refresh();
					logger.info("Imported standings");
				}
			}
			logger.info("Finished import of data to database");
		} catch (InvalidYearException e) {
			logger.error("Could not import data to the database because current year is not valid");
		}
	}

	private boolean areStandingsUpToDate(Year year) {
		try {
			RaceId latestRaceId = db.getLatestRaceResultId(year);
			try {
				RaceId standingsRaceId = db.getLatestStandingsId(year);
				return latestRaceId.equals(standingsRaceId);
			} catch (EmptyResultDataAccessException e) {
				return false;
			}
		} catch (EmptyResultDataAccessException e) {
			return true;
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
		logger.info("Race data from '{}' manually reloaded by admin", raceId);
		importStartingGridData(raceId);
		importRaceResultData(raceId);
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			RaceId newestRaceId = db.getLatestRaceId(year);
			if (raceId.equals(newestRaceId)) {
				logger.info("Race that was manually reloaded is the newest race. Will import standings as well");
				importStandings(year, new Points());
			}
		} catch (InvalidYearException e) {
		} catch (EmptyResultDataAccessException e) {
		}
	}

	private void importStartingGridData(RaceId raceId) {
		List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId.value);
		if (startingGrid.isEmpty()) {
			return;
		}
		insertStartingGridData(raceId, startingGrid);
	}

	private ResultChangeStatus importRaceResultData(RaceId raceId) {
		List<List<String>> raceResult = TableImporter.getRaceResult(raceId.value);
		if (raceResult.isEmpty()) {
			return ResultChangeStatus.NO_CHANGE;
		}
		List<PositionedCompetitor> preList = db.getRaceResult(raceId);
		insertRaceResultData(raceId, raceResult);
		List<PositionedCompetitor> postList = db.getRaceResult(raceId);
		if (preList.size() != postList.size()) {
			ResultChangeStatus status = ResultChangeStatus.POINTS_CHANGE;
			Points change = compPoints(postList);
			status.setPointsChange(change);
			return status;
		}
		for (int i = 0; i < preList.size(); i++) {
			PositionedCompetitor pre = preList.get(i);
			PositionedCompetitor post = postList.get(i);
			if (!pre.equals(post)) {
				if (i < 10) {
					return ResultChangeStatus.POINTS_CHANGE;
				}
				return ResultChangeStatus.OUTSIDE_POINTS_CHANGE;
			}
		}
		return ResultChangeStatus.NO_CHANGE;
	}

	private ResultChangeStatus refreshLatestImports(Year year) {
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

	private ResultChangeStatus refreshLatestRaceResult(Year year) {
		try {
			RaceId raceId = db.getLatestRaceResultId(year);
			return importRaceResultData(raceId);
		} catch (EmptyResultDataAccessException e) {
			return ResultChangeStatus.NO_CHANGE;
		}
	}

	private int importStartingGrids(List<RaceId> racesToImportFrom) {
		int count = 0;
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
			count++;
		}
		return count;
	}

	private void insertStartingGridData(RaceId raceId, List<List<String>> startingGrid) {
		for (List<String> row : startingGrid.subList(1, startingGrid.size())) {
			int position = Integer.parseInt(row.get(0));
			String driver = parseDriver(row.get(2), raceId);
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
			insertRaceResultData(raceId, raceResult);
			addedNewRace = true;
		}
		return addedNewRace;
	}

	private void insertRaceResultData(RaceId raceId, List<List<String>> raceResult) {
		int finishingPosition = 1;
		List<List<String>> disqualified = new ArrayList<>();
		for (List<String> row : raceResult.subList(1, raceResult.size())) {
			String position = row.get(0);
			if (position.equals("DQ")) {
				disqualified.add(row);	
				continue;
			}
			insertRaceResultRow(raceId, row, finishingPosition++);
		}
		for (List<String> row : disqualified) {
			insertRaceResultRow(raceId, row, finishingPosition++);
		}
	}

	private void insertRaceResultRow(RaceId raceId, List<String> row, int finishingPosition) {
		String position = row.get(0);
		Driver driver = new Driver(parseDriver(row.get(2), raceId), db);
		Points points = new Points((int) Double.parseDouble(row.get(6)));
		db.insertDriverRaceResult(raceId, position, driver, points, finishingPosition);
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

	private boolean importStandings(Year year, Points expectedChange) {
		try {
			RaceId newestRace = db.getLatestRaceId(year);
			ResultChangeStatus driverStatus = importDriverStandings(year, newestRace);
			ResultChangeStatus constructorStatus = importConstructorStandings(year, newestRace);
			boolean equalPointsChange = driverStatus.getPointsChange().equals(constructorStatus.getPointsChange());
			boolean driverValidStatus = validResultStatus(driverStatus, expectedChange);
			boolean constructorValidStatus = validResultStatus(constructorStatus, expectedChange);
			return equalPointsChange && driverValidStatus && constructorValidStatus;
		} catch (EmptyResultDataAccessException e) {
			throw new RuntimeException("Should not call importStandings without having a race result");
		}
	}

	private boolean validResultStatus(ResultChangeStatus status, Points expectedChange) {
		boolean hasChanged = status == ResultChangeStatus.POINTS_CHANGE;
		boolean greatEnoughChange = status.getPointsChange().compareTo(expectedChange) >= 0;
		return hasChanged && greatEnoughChange; 
	}

	private ResultChangeStatus importDriverStandings(Year year, RaceId newestRace) {
		List<List<String>> standings = TableImporter.getDriverStandings(year.value);
		if (standings.size() == 0) {
			logger.info("Driver standings not available");
			return ResultChangeStatus.NO_CHANGE;
		}
		standings = standings.subList(1, standings.size());
		List<PositionedCompetitor> currentStandings = standings.stream()
			.map(row -> 
			new PositionedCompetitor(
				String.valueOf(Integer.parseInt(row.get(0))),
				parseDriver(row.get(1), year), 
				String.valueOf((int) Double.parseDouble(row.get(4)))
				))
			.toList();
		ResultChangeStatus status = isDriverStandingsNew(currentStandings, year);
		if (status != ResultChangeStatus.POINTS_CHANGE) {
			logger.info("Driver standings are not new, will not add new");
			return ResultChangeStatus.NO_CHANGE;
		}
		for (PositionedCompetitor competitor : currentStandings) {
			Driver driver = new Driver(competitor.name, db);
			int position = Integer.parseInt(competitor.position);
			Points points = new Points(Integer.parseInt(competitor.points));
			db.insertDriverIntoStandings(newestRace, driver, position, points);
		}
		logger.info("Driver standings added for race '{}'", newestRace);
		return status;
	}

	private ResultChangeStatus isDriverStandingsNew(List<PositionedCompetitor> standings, Year year) {
		try {
			RaceId previousRaceId = db.getLatestStandingsId(year);
			List<PositionedCompetitor> previousStandings = db.getDriverStandings(previousRaceId);
			return compareStandings(standings, previousStandings);
		} catch (EmptyResultDataAccessException e) {
			return changeWithSum(standings);
		}
	}

	private ResultChangeStatus importConstructorStandings(Year year, RaceId newestRace) {
		List<List<String>> standings = TableImporter.getConstructorStandings(year.value);
		if (standings.size() == 0) {
			logger.info("Constructor standings not available");
			return ResultChangeStatus.NO_CHANGE;
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
		ResultChangeStatus status = isConstructorStandingsNew(currentStandings, year);
		if (status != ResultChangeStatus.POINTS_CHANGE) {
			logger.info("Constructor standings are not new, will not add new");
			return ResultChangeStatus.NO_CHANGE;
		}
		for (PositionedCompetitor competitor : currentStandings) {
			int position = Integer.parseInt(competitor.position);
			Points points = new Points(Integer.parseInt(competitor.points));
			Constructor validConstructor = new Constructor(competitor.name);
			db.insertConstructorIntoStandings(newestRace, validConstructor, position, points);
		}
		logger.info("Constructor standings added for race '{}'", newestRace);
		return status;
	}

	private ResultChangeStatus isConstructorStandingsNew(List<PositionedCompetitor> standings, Year year) {
		try {
			RaceId previousRaceId = db.getLatestStandingsId(year);
			List<PositionedCompetitor> previousStandings = db.getConstructorStandings(previousRaceId);
			return compareStandings(standings, previousStandings);
		} catch (EmptyResultDataAccessException e) {
			return changeWithSum(standings);
		}
	}

	private ResultChangeStatus compareStandings(List<PositionedCompetitor> standings, List<PositionedCompetitor> previousStandings) {
		ResultChangeStatus status = null;
		if (standings.size() != previousStandings.size()) {
			status = ResultChangeStatus.POINTS_CHANGE;
		} else {
			for (int i = 0; i < standings.size(); i++) {
				PositionedCompetitor previousCompetitor = previousStandings.get(i);
				PositionedCompetitor competitor = standings.get(i);
				if (!previousCompetitor.equals(competitor)) {
					status = ResultChangeStatus.POINTS_CHANGE;
					break;
				}
			}
			if (status == null) {
				status = ResultChangeStatus.NO_CHANGE;
			}
		}
		int diff = compPoints(standings).value - compPoints(previousStandings).value;
		status.setPointsChange(new Points(diff));
		return status;
	}

	private ResultChangeStatus changeWithSum(List<PositionedCompetitor> competitors) {
		ResultChangeStatus status = ResultChangeStatus.POINTS_CHANGE;
		Points change = compPoints(competitors);
		status.setPointsChange(change);
		return status;
	}

	private Points compPoints(List<PositionedCompetitor> competitors) {
		return competitors.stream()
			.map(comp -> new Points(Integer.parseInt(comp.points)))
			.reduce(new Points(), (points1, points2) -> points1.add(points2));
	}

	private String parseDriver(String driverName) {
		return driverName.substring(0, driverName.length() - 3);
	}

	private String parseDriver(String driverName, RaceId raceId) {
		return db.getAlternativeDriverName(parseDriver(driverName), raceId);
	}

	private String parseDriver(String driverName, Year year) {
		return db.getAlternativeDriverName(parseDriver(driverName), year);
	}

	private enum ResultChangeStatus {
		NO_CHANGE,
		POINTS_CHANGE, 
		OUTSIDE_POINTS_CHANGE;

		private Points changeInPoints = new Points();

		public void setPointsChange(Points changeInPoints) {
			this.changeInPoints = changeInPoints;
		}
		
		public Points getPointsChange() {
			return changeInPoints;
		}
	}
}
