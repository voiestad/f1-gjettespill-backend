package no.vebb.f1.scoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;

public class UserScore {

	private Database db;

	private final UUID id;
	private final Year year;
	private Points score = new Points();
	private final RaceId raceId;
	private final int racePos;
	public final Table driversTable;
	public final Table constructorsTable;
	public final Table flagsTable;
	public final Table winnerTable;
	public final Table tenthTable;
	public final Table summaryTable;
	private final List<List<String>> summaryTableBody = new ArrayList<>();

	public UserScore(UUID id, Year year, RaceId raceId, Database db) {
		this.id = id;
		this.year = year;
		this.raceId = raceId;
		this.db = db;
		this.racePos = getRacePosition();
		this.driversTable = initializeDriversTable();
		this.constructorsTable = initializeConstructorsTable();
		this.flagsTable = initializeFlagsTable();
		this.winnerTable = initializeWinnerTable();
		this.tenthTable = initializeTenthTable();
		this.summaryTable = initializeSummaryTable();
	}

	public UserScore(UUID id, Year year, Database db) {
		this(id, year, UserScore.getRaceId(year, db), db);
	}

	private static RaceId getRaceId(Year year, Database db) {
		try {
			return db.getLatestRaceId(year);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private int getRacePosition() {
		if (raceId == null) {
			return 0;
		}
		return db.getPositionOfRace(raceId);
	}

	private Table initializeDriversTable() {
		Category category = new Category("DRIVER", db);
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Gjettet", "Diff", "Poeng");

		List<Driver> guessedDriver = db.getGuessedYearDriver(year, id);
		List<Driver> drivers = db.getDriverStandings(raceId, year);
		return getGuessedToPos(map, category, header, guessedDriver, drivers);
	}

	private Table initializeConstructorsTable() {
		Category category = new Category("CONSTRUCTOR", db);
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		List<String> header = Arrays.asList("Plass", "Konstruktør", "Gjettet", "Diff", "Poeng");

		List<Constructor> guessedConstructor = db.getGuessedYearConstructor(year, id);
		List<Constructor> constructors = db.getConstructorStandings(raceId, year);

		return getGuessedToPos(map, category, header, guessedConstructor, constructors);
	}

	private <T> Table getGuessedToPos(DiffPointsMap map, Category category, List<String> header, List<T> guessed, List<T> competitors) {
		List<List<String>> body = new ArrayList<>();
		Points competitorScore = new Points();
		Map<T, Integer> guessedToPos = new HashMap<>();
		for (int i = 0; i < guessed.size(); i++) {
			guessedToPos.put(guessed.get(i), i+1);
		}
		for (int i = 0; i < competitors.size(); i++) {
			List<String> row = new ArrayList<>();
			int actualPos = i + 1;
			row.add(String.valueOf(actualPos));
			T competitor = competitors.get(i);
			row.add(competitor.toString());
			Integer pos = guessedToPos.get(competitor);
			if (pos == null) {
				row.add("N/A");
				row.add("N/A");
				row.add("0");
			} else {
				Diff diff = new Diff(Math.abs(actualPos - pos)); 
				Points points = map.getPoints(diff);
				competitorScore = competitorScore.add(points);
				row.add(pos.toString());
				row.add(String.valueOf(diff));
				row.add(String.valueOf(points));
				
			}
			body.add(row);
		}
		score = score.add(competitorScore);
		String translation = db.translateCategory(category);
		summaryTableBody.add(Arrays.asList(translation, String.valueOf(competitorScore)));
		return new Table(translation, header, body);
	}

	private Table initializeFlagsTable() {
		Category category = new Category("FLAG", db);
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		List<String> header = Arrays.asList("Type", "Gjettet", "Faktisk", "Diff", "Poeng");
		List<List<String>> body = new ArrayList<>();
		Points flagScore = new Points();

		List<Map<String, Object>> sqlRes = db.getDataForFlagTable(racePos, year, id);

		for (Map<String, Object> row : sqlRes) {
			Flag flag = new Flag((String) row.get("type"), db);
			String translatedFlag = db.translateFlagName(flag);
			int guessed = (int) row.get("guessed");
			int actual = (int) row.get("actual");
			Diff diff = new Diff(Math.abs(guessed - actual));
			Points points = map.getPoints(diff);
			flagScore = flagScore.add(points);
			body.add(Arrays.asList(translatedFlag, String.valueOf(guessed), String.valueOf(actual), String.valueOf(diff), String.valueOf(points))); 
		}

		Collections.sort(body, (a, b) -> a.get(0).compareTo(b.get(0)));

		score = score.add(flagScore);
		String translation = db.translateCategory(category);
		summaryTableBody.add(Arrays.asList(translation, String.valueOf(flagScore)));
		return new Table(translation, header, body);
	}
	
	private Table initializeWinnerTable() {
		Category category = new Category("FIRST", db);
		return getDriverPlaceGuessTable(category, 1);
	}
	
	private Table initializeTenthTable() {
		Category category = new Category("TENTH", db);
		return getDriverPlaceGuessTable(category, 10);
	}

	private Table getDriverPlaceGuessTable(Category category, int targetPos) {
		List<String> header = Arrays.asList("Løp", "Gjettet", "Startet", "Plass", "Poeng");
		List<List<String>> body = new ArrayList<>();
		String translation = db.translateCategory(category);
		if (raceId == null) {
			return new Table(translation, header, body);
		}
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		Points driverPlaceScore = new Points();
		List<Map<String, Object>> sqlRes = db.getDataForPlaceGuessTable(category, id, year, racePos);

		for (Map<String, Object> row : sqlRes) {
			String raceName = (String) row.get("race_name");
			String driver = (String) row.get("driver");
			int startPos = (int) row.get("start");
			int finishPos = (int) row.get("finish");
			Diff diff = new Diff(Math.abs(targetPos - finishPos));
			Points points = map.getPoints(diff);
			driverPlaceScore = driverPlaceScore.add(points);
			body.add(Arrays.asList(raceName, driver, String.valueOf(startPos), String.valueOf(finishPos), String.valueOf(points)));
		}
		score = score.add(driverPlaceScore);

		summaryTableBody.add(Arrays.asList(translation, String.valueOf(driverPlaceScore)));
		return new Table(translation, header, body);
	}
	
	private Table initializeSummaryTable() {
		List<String> header = Arrays.asList("Kategori", "Poeng");
		return new Table("Oppsummering", header, summaryTableBody);
	}

	public Points getScore() {
		return score;
	}

	public List<Table> getAllTables() {
		List<Table> tables = Arrays.asList(summaryTable, driversTable, constructorsTable, flagsTable, winnerTable, tenthTable);
		return tables;
	}
}
