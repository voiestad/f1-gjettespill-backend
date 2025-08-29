package no.vebb.f1.importing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.io.StringWriter;
import java.io.PrintWriter;

import no.vebb.f1.mail.MailService;
import no.vebb.f1.race.RaceOrderEntity;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.scoring.ScoreCalculator;
import no.vebb.f1.year.YearService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.TimeUtil;
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

	private final Database db;
	private final MailService mailService;
	private final ScoreCalculator scoreCalculator;
	private final ResultService resultService;
	private final YearService yearService;
	private final RaceService raceService;

	public Importer(Database db, MailService mailService, ScoreCalculator scoreCalculator, ResultService resultService, YearService yearService, RaceService raceService) {
		this.db = db;
		this.mailService = mailService;
		this.scoreCalculator = scoreCalculator;
		this.resultService = resultService;
		this.yearService = yearService;
		this.raceService = raceService;
	}

	@Transactional
	public void importData() {
		logger.info("Starting import of data to database");
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
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
						logger.info("Standings were not new.");
						throw new RuntimeException("Standings were not up to date with race result.");	
					} else {
						scoreCalculator.calculateScores();
						logger.info("Race result changed outside points without standings changing. Sending message to admins.");
						mailService.sendServerMessageToAdmins(
							"Endringer i resultat av løp utenfor poengene uten at mesterskapet endret seg. Vennligst verifiser at mesterskapet er korrekt.");
					}
				} else {
					scoreCalculator.calculateScores();
					logger.info("Imported standings");
				}
			}
			logger.info("Finished import of data to database");
		} catch (InvalidYearException e) {
			logger.error("Could not import data to the database because current year is not valid");
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			String errorMessage = stringWriter.toString();
			logger.error("Exception while importing. Rolling back.\n{}", errorMessage);
		}
	}

	private boolean areStandingsUpToDate(Year year) {
		try {
			RaceId latestRaceId = raceService.getLatestRaceId(year);
			try {
				RaceId standingsRaceId = raceService.getLatestStandingsId(year);
				return latestRaceId.equals(standingsRaceId);
			} catch (InvalidYearException e) {
				return false;
			}
		} catch (InvalidYearException e) {
			return true;
		}
	}

	private Map<Year, List<RaceId>> getActiveRaces() {
		Map<Year, List<RaceId>> activeRaces = new LinkedHashMap<>();
		List<RaceOrderEntity> sqlRes = raceService.getActiveRaces();
		for (RaceOrderEntity race : sqlRes) {
			Year year = new Year(race.year());
			if (!activeRaces.containsKey(year)) {
				activeRaces.put(year, new ArrayList<>());
			}
			List<RaceId> races = activeRaces.get(year);
			races.add(new RaceId(race.raceId()));
		}
		return activeRaces;
	}

	public void importRaceData(RaceId raceId) {
		logger.info("Race data from '{}' manually reloaded by admin", raceId);
		importStartingGridData(raceId);
		ResultChangeStatus changeStatus = importRaceResultData(raceId);
		if (!changeStatus.equals(ResultChangeStatus.NO_CHANGE)) {
			logger.info("Changes to the race result");
		} else {
			logger.info("No change in race result");
		}
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), yearService);
			RaceId newestRaceId = raceService.getLatestRaceId(year);
			if (raceId.equals(newestRaceId)) {
				logger.info("Race that was manually reloaded is the newest race. Will import standings as well");
				importStandings(year, new Points());
			}
		} catch (InvalidYearException | EmptyResultDataAccessException ignored) {
		}
		scoreCalculator.calculateScores();
	}

	private void importStartingGridData(RaceId raceId) {
		List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId.value);
		if (startingGrid.size() <= 1) {
			return;
		}
		insertStartingGridData(raceId, startingGrid);
	}

	private ResultChangeStatus importRaceResultData(RaceId raceId) {
		List<List<String>> raceResult = TableImporter.getRaceResult(raceId.value);
		if (raceResult.size() <= 1) {
			return ResultChangeStatus.NO_CHANGE;
		}
		List<PositionedCompetitor> preList = resultService.getRaceResult(raceId).stream().map(PositionedCompetitor::fromRaceResult).toList();
		insertRaceResultData(raceId, raceResult);
		List<PositionedCompetitor> postList = resultService.getRaceResult(raceId).stream().map(PositionedCompetitor::fromRaceResult).toList();
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
			RaceId raceId = raceService.getLatestStartingGridRaceId(year);
			importStartingGridData(raceId);
		} catch (InvalidYearException ignored) {
		}
	}

	private ResultChangeStatus refreshLatestRaceResult(Year year) {
		try {
			RaceId raceId = raceService.getLatestRaceId(year);
			return importRaceResultData(raceId);
		} catch (InvalidYearException e) {
			return ResultChangeStatus.NO_CHANGE;
		}
	}

	private int importStartingGrids(List<RaceId> racesToImportFrom) {
		int count = 0;
		for (RaceId raceId : racesToImportFrom) {
			boolean isAlreadyAdded = resultService.isStartingGridAdded(raceId);
			if (isAlreadyAdded) {
				continue;
			}
			List<List<String>> startingGrid = TableImporter.getStartingGrid(raceId.value);
			if (startingGrid.size() <= 1) {
				break;
			}
			insertStartingGridData(raceId, startingGrid);
			count++;
		}
		return count;
	}

	private void insertStartingGridData(RaceId raceId, List<List<String>> startingGrid) {
		try {
			for (List<String> row : startingGrid.subList(1, startingGrid.size())) {
				int position = Integer.parseInt(row.get(0));
				String driver = parseDriver(row.get(2), raceId);
				db.addDriver(driver);
				Driver validDriver = new Driver(driver, db);
				resultService.insertDriverStartingGrid(raceId, position, validDriver);
			}

		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			throw new RuntimeException("Failed parsing starting grid for race " + raceId + ".\n" + getTableString(startingGrid));
		}
	}

	private boolean importRaceResults(List<RaceId> racesToImportFrom) {
		boolean addedNewRace = false;
		for (RaceId raceId : racesToImportFrom) {
			boolean isAlreadyAdded = resultService.isRaceResultAdded(raceId);
			if (isAlreadyAdded) {
				throw new RuntimeException("Race is already added and was attempted added again");
			}
			List<List<String>> raceResult = TableImporter.getRaceResult(raceId.value);
			if (raceResult.size() <= 1) {
				break;
			}
			insertRaceResultData(raceId, raceResult);
			addedNewRace = true;
		}
		return addedNewRace;
	}

	private void insertRaceResultData(RaceId raceId, List<List<String>> raceResult) {
		try {
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
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			throw new RuntimeException("Failed parsing race result for race " + raceId + ".\n" + getTableString(raceResult));
		}
	}

	private void insertRaceResultRow(RaceId raceId, List<String> row, int finishingPosition) {
		String position = row.get(0);
		Driver driver = new Driver(parseDriver(row.get(2), raceId), db);
		Points points = new Points((int) Double.parseDouble(row.get(6)));
		resultService.insertDriverRaceResult(raceId, position, driver, points, finishingPosition);
	}

	public void importRaceNames(List<Integer> racesToImportFrom, Year year) {
		int position = 1;
		for (int raceId : racesToImportFrom) {
			if (addRace(raceId, year, position)) {
				position++;
			}
		}
	}

	public void importRaceName(int raceId, Year year) {
		int position = raceService.getMaxRaceOrderPosition(year) + 1;
		addRace(raceId, year, position);
	}

	private boolean addRace(int raceId, Year year, int position) {
		boolean isAlreadyAdded = raceService.isRaceAdded(raceId);
		if (isAlreadyAdded) {
			throw new RuntimeException("Race name was already added");
		}
		String raceName = TableImporter.getGrandPrixName(raceId);
		// TODO: Use exception and not empty string check
		if (raceName.isEmpty()) {
			return false;
		}
		raceService.insertRace(raceId, raceName);
		RaceId validRaceId = new RaceId(raceId, raceService);
		raceService.insertRaceOrder(validRaceId, year, position);
		return true;
	}

	private boolean importStandings(Year year, Points expectedChange) {
		try {
			RaceId newestRace = raceService.getLatestRaceId(year);
			ResultChangeStatus driverStatus = importDriverStandings(year, newestRace);
			ResultChangeStatus constructorStatus = importConstructorStandings(year, newestRace);
			boolean equalPointsChange = driverStatus.getPointsChange().equals(constructorStatus.getPointsChange());
			boolean driverValidStatus = validResultStatus(driverStatus, expectedChange);
			boolean constructorValidStatus = validResultStatus(constructorStatus, expectedChange);
			return equalPointsChange && driverValidStatus && constructorValidStatus;
		} catch (EmptyResultDataAccessException | InvalidYearException e) {
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
		if (standings.size() <= 1) {
			logger.info("Driver standings not available");
			return ResultChangeStatus.NO_CHANGE;
		}
		standings = standings.subList(1, standings.size());
		try {
			List<PositionedCompetitor> currentStandings = standings.stream()
				.map(row -> 
				new PositionedCompetitor(
					String.valueOf(Integer.parseInt(row.get(0))),
					parseDriver(row.get(1), year), 
					(int) Double.parseDouble(row.get(4))
					))
				.toList();
			ResultChangeStatus status = isDriverStandingsNew(currentStandings, year);
			if (status != ResultChangeStatus.POINTS_CHANGE) {
				logger.info("Driver standings are not new, will not add new");
				return ResultChangeStatus.NO_CHANGE;
			}
			for (PositionedCompetitor competitor : currentStandings) {
				Driver driver = new Driver(competitor.name(), db);
				int position = Integer.parseInt(competitor.position());
				Points points = new Points(competitor.points());
				resultService.insertDriverIntoStandings(newestRace, driver, position, points);
			}
			logger.info("Driver standings added for race '{}'", newestRace);
			return status;
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			throw new RuntimeException("Failed parsing driver standings.\n" + getTableString(standings));
		}
	}

	private ResultChangeStatus isDriverStandingsNew(List<PositionedCompetitor> standings, Year year) {
		try {
			RaceId previousRaceId = raceService.getLatestStandingsId(year);
			List<PositionedCompetitor> previousStandings = resultService.getDriverStandings(previousRaceId).stream().map(PositionedCompetitor::fromDriverStandings).toList();
			return compareStandings(standings, previousStandings);
		} catch (EmptyResultDataAccessException e) {
			return changeWithSum(standings);
		}
	}

	private ResultChangeStatus importConstructorStandings(Year year, RaceId newestRace) {
		List<List<String>> standings = TableImporter.getConstructorStandings(year.value);
		if (standings.size() <= 1) {
			logger.info("Constructor standings not available");
			return ResultChangeStatus.NO_CHANGE;
		}
		standings = standings.subList(1, standings.size());
		try {
			List<PositionedCompetitor> currentStandings = standings.stream()
				.map(row -> 
				new PositionedCompetitor(
					String.valueOf(Integer.parseInt(row.get(0))),
					row.get(1),
					(int) Double.parseDouble(row.get(2))
					))
				.toList();
			ResultChangeStatus status = isConstructorStandingsNew(currentStandings, year);
			if (status != ResultChangeStatus.POINTS_CHANGE) {
				logger.info("Constructor standings are not new, will not add new");
				return ResultChangeStatus.NO_CHANGE;
			}
			for (PositionedCompetitor competitor : currentStandings) {
				int position = Integer.parseInt(competitor.position());
				Points points = new Points(competitor.points());
				Constructor validConstructor = new Constructor(competitor.name());
				resultService.insertConstructorIntoStandings(newestRace, validConstructor, position, points);
			}
			logger.info("Constructor standings added for race '{}'", newestRace);
			return status;
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			throw new RuntimeException("Failed parsing constructor standings.\n" + getTableString(standings));
		}
	}

	private ResultChangeStatus isConstructorStandingsNew(List<PositionedCompetitor> standings, Year year) {
		try {
			RaceId previousRaceId = raceService.getLatestStandingsId(year);
			List<PositionedCompetitor> previousStandings = resultService.getConstructorStandings(previousRaceId).stream().map(PositionedCompetitor::fromConstructorStandings).toList();
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
		int diff = Math.abs(compPoints(standings).value - compPoints(previousStandings).value);
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
			.map(comp -> new Points(comp.points()))
			.reduce(new Points(), Points::add);
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

	private String getTableString(List<List<String>> table) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < table.size(); i++) {
			buffer.append("Row: ").append(i+1).append('\n').append(table.get(i)).append('\n');
		}
		return buffer.toString();
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
