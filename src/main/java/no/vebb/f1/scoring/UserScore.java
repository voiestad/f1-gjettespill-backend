package no.vebb.f1.scoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private final RaceId raceId;
	private final int racePos;
	private final List<StandingsGuess<Driver>> driversGuesses = new ArrayList<>();
	private final List<StandingsGuess<Constructor>> constructorsGuesses = new ArrayList<>();
	private final List<FlagGuess> flagGuesses = new ArrayList<>();
	private final List<PlaceGuess> winnerGuesses = new ArrayList<>();
	private final List<PlaceGuess> tenthGuesses = new ArrayList<>();
	private final Summary summary = new Summary();

	private final Category driverCat;
	private final Category constructorCat;
	private final Category flagCat;
	private final Category winnerCat;
	private final Category tenthCat;

	public UserScore(UUID id, Year year, RaceId raceId, Database db) {
		this.driverCat = new Category("DRIVER", db);
		this.constructorCat = new Category("CONSTRUCTOR", db);
		this.flagCat = new Category("FLAG", db);
		this.winnerCat = new Category("FIRST", db);
		this.tenthCat = new Category("TENTH", db);
		this.id = id;
		this.year = year;
		this.raceId = raceId;
		this.db = db;
		this.racePos = getRacePosition();
		initializeDriversGuesses();
		initializeConstructorsGuesses();
		initializeFlagsGuesses();
		initializeWinnerGuesses();
		initializeTenthGuesses();
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

	private Table getDriversTable() {
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Gjettet", "Diff", "Poeng");
		return getStandingsTable(driversGuesses, header, driverCat);
	}

	private Table getConstructorsTable() {
		List<String> header = Arrays.asList("Plass", "Konstruktør", "Gjettet", "Diff", "Poeng");
		return getStandingsTable(constructorsGuesses, header, constructorCat);
	}

	private Table getFlagTable() {
		List<String> header = Arrays.asList("Type", "Gjettet", "Faktisk", "Diff", "Poeng");
		String translation = db.translateCategory(flagCat);
		List<List<String>> body = flagGuesses.stream()
			.map(guess -> Arrays.asList(
				db.translateFlagName(guess.flag()),
				String.valueOf(guess.guessed()),
				String.valueOf(guess.actual()),
				guess.diff().toString(),
				guess.points().toString()
			))
			.sorted((a, b) -> a.get(0).compareTo(b.get(0)))
			.toList();
		return new Table(translation, header, body);
	}

	private Table getWinnerTable() {
		return getPlaceGuessTable(winnerCat, winnerGuesses);
	}

	private Table getTenthTable() {
		return getPlaceGuessTable(tenthCat, tenthGuesses);
	}

	private Table getSummaryTable() {
		List<String> header = Arrays.asList("Kategori", "Poeng");
		List<List<String>> body = new ArrayList<>();
		body.add(Arrays.asList(db.translateCategory(driverCat), summary.drivers.toString()));
		body.add(Arrays.asList(db.translateCategory(constructorCat), summary.constructors.toString()));
		body.add(Arrays.asList(db.translateCategory(flagCat), summary.flag.toString()));
		body.add(Arrays.asList(db.translateCategory(winnerCat), summary.winner.toString()));
		body.add(Arrays.asList(db.translateCategory(tenthCat), summary.tenth.toString()));
		body.add(Arrays.asList("Totalt", summary.getTotal().toString()));
		return new Table("Oppsummering", header, body);
	}
	
	private <T> Table getStandingsTable(List<StandingsGuess<T>> guesses, List<String> header, Category category) {
		List<List<String>> body = guesses.stream()
			.map(guess -> guess.guessed() != null ? 
				Arrays.asList(
					String.valueOf(guess.pos()),
					guess.competitor().toString(),
					guess.guessed().toString(),
					guess.diff().toString(),
					guess.points().toString()
				)
				: Arrays.asList(
					String.valueOf(guess.pos()),
					guess.competitor().toString(),
					"N/A",
					"N/A",
					guess.points().toString()
				)
			)
			.toList();
		String translation = db.translateCategory(category);
		return new Table(translation, header, body);
	}

	private Table getPlaceGuessTable(Category category, List<PlaceGuess> guesses) {
		List<String> header = Arrays.asList("Løp", "Gjettet", "Startet", "Plass", "Poeng");
		String translation = db.translateCategory(category);
		List<List<String>> body = guesses.stream()
			.map(guess -> Arrays.asList(
				guess.raceName(),
				guess.driver(),
				String.valueOf(guess.startPos()),
				String.valueOf(guess.finishPos()),
				guess.points().toString()
			))
			.toList();
		return new Table(translation, header, body);
	}

	private void initializeDriversGuesses() {
		List<Driver> guessedDriver = db.getGuessedYearDriver(year, id);
		List<Driver> drivers = db.getDriverStandings(raceId, year);
		summary.drivers = getGuessedToPos(driverCat, guessedDriver, drivers, driversGuesses);
	}

	private void initializeConstructorsGuesses() {
		List<Constructor> guessedConstructor = db.getGuessedYearConstructor(year, id);
		List<Constructor> constructors = db.getConstructorStandings(raceId, year);
		summary.constructors = getGuessedToPos(constructorCat, guessedConstructor, constructors, constructorsGuesses);
	}

	private <T> Points getGuessedToPos(Category category, List<T> guessed,
			List<T> competitors, List<StandingsGuess<T>> result) {
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		Points competitorScore = new Points();
		Map<T, Integer> guessedToPos = new HashMap<>();
		for (int i = 0; i < guessed.size(); i++) {
			guessedToPos.put(guessed.get(i), i + 1);
		}
		for (int i = 0; i < competitors.size(); i++) {
			int actualPos = i + 1;
			T competitor = competitors.get(i);
			Integer guessedPos = guessedToPos.get(competitor);
			if (guessedPos == null) {
				result.add(new StandingsGuess<T>(actualPos, competitor, null, null, new Points()));
			} else {
				Diff diff = new Diff(Math.abs(actualPos - guessedPos));
				Points points = map.getPoints(diff);
				result.add(new StandingsGuess<T>(actualPos, competitor, guessedPos, diff, points));
				competitorScore = competitorScore.add(points);
			}
		}
		return competitorScore;
	}

	private void initializeFlagsGuesses() {
		DiffPointsMap map = new DiffPointsMap(flagCat, year, db);
		Points flagScore = new Points();

		List<Map<String, Object>> sqlRes = db.getDataForFlagTable(racePos, year, id);
		for (Map<String, Object> row : sqlRes) {
			Flag flag = new Flag((String) row.get("type"), db);
			int guessed = (int) row.get("guessed");
			int actual = (int) row.get("actual");
			Diff diff = new Diff(Math.abs(guessed - actual));
			Points points = map.getPoints(diff);
			flagScore = flagScore.add(points);
			flagGuesses.add(new FlagGuess(flag, guessed, actual, diff, points));
		}
		summary.flag = flagScore;
	}

	private void initializeWinnerGuesses() {
		summary.winner = getDriverPlaceGuessTable(winnerCat, 1, winnerGuesses);
	}

	private void initializeTenthGuesses() {
		summary.tenth = getDriverPlaceGuessTable(tenthCat, 10, tenthGuesses);
	}

	private Points getDriverPlaceGuessTable(Category category, int targetPos, List<PlaceGuess> result) {
		if (raceId == null) {
			return new Points();
		}
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		Points driverPlaceScore = new Points();
		List<Map<String, Object>> sqlRes = db.getDataForPlaceGuessTable(category, id, year, racePos);

		for (Map<String, Object> row : sqlRes) {
			int racePosition = (int) row.get("race_position");
			String raceName = (String) row.get("race_name");
			String driver = (String) row.get("driver");
			int startPos = (int) row.get("start");
			int finishPos = (int) row.get("finish");
			Diff diff = new Diff(Math.abs(targetPos - finishPos));
			Points points = map.getPoints(diff);
			driverPlaceScore = driverPlaceScore.add(points);
			result.add(new PlaceGuess(racePosition, raceName, driver, startPos, finishPos, diff, points));
		}
		return driverPlaceScore;
	}

	public Points getScore() {
		return summary.getTotal();
	}

	public List<Table> getAllTables() {
		List<Table> tables = Arrays.asList(
			getSummaryTable(),
			getDriversTable(),
			getConstructorsTable(),
			getFlagTable(),
			getWinnerTable(),
			getTenthTable());
		return tables;
	}

	public List<Table> comparisonTables(UserScore other) {
		if (!Objects.equals(raceId, other.raceId)) {
			throw new RuntimeException("Compared UserScore needs to be from the same raceId");
		}
		if (!year.equals(other.year)) {
			throw new RuntimeException("Compared UserScore needs to be from the same year");
		}
		List<Table> tables = new ArrayList<>();
		tables.add(getSummaryTableComparison(other));
		tables.add(getDriversTableComparison(other));
		tables.add(getConstructorsTableComparison(other));
		tables.add(getFlagTableComparison(other));
		tables.add(getWinnerTableComparison(other));
		tables.add(getTenthTableComparison(other));
		return tables;
	}

	private Table getSummaryTableComparison(UserScore other) {
		List<String> header = Arrays.asList("Poeng", "Kategori", "Poeng");
		List<List<String>> body = new ArrayList<>();
		body.add(Arrays.asList(summary.drivers.toString(), db.translateCategory(driverCat), other.summary.drivers.toString()));
		body.add(Arrays.asList(summary.constructors.toString(), db.translateCategory(constructorCat), other.summary.constructors.toString()));
		body.add(Arrays.asList(summary.flag.toString(), db.translateCategory(flagCat), other.summary.flag.toString()));
		body.add(Arrays.asList(summary.winner.toString(), db.translateCategory(winnerCat), other.summary.winner.toString()));
		body.add(Arrays.asList(summary.tenth.toString(), db.translateCategory(tenthCat), other.summary.tenth.toString()));
		body.add(Arrays.asList(summary.getTotal().toString(), "Totalt", other.summary.getTotal().toString()));
		return new Table("Oppsummering", header, body);
	}

	private Table getDriversTableComparison(UserScore other) {
		List<String> header = Arrays.asList("Poeng", "Gjettet", "Plass", "Sjåfør", "Gjettet", "Poeng");
		return getStandingsTableComparison(driversGuesses, other.driversGuesses, header, driverCat);
	}
	
	private Table getConstructorsTableComparison(UserScore other) {
		List<String> header = Arrays.asList("Poeng", "Gjettet", "Plass", "Sjåfør", "Gjettet", "Poeng");
		return getStandingsTableComparison(constructorsGuesses, other.constructorsGuesses, header, constructorCat);
	}
	
	private Table getFlagTableComparison(UserScore other) {
		List<String> header = Arrays.asList("Poeng", "Gjettet", "Faktisk", "Type", "Gjettet", "Poeng");
		String translation = db.translateCategory(flagCat);
		List<List<String>> body = new ArrayList<>();
		int maxFlags = Math.max(flagGuesses.size(), other.flagGuesses.size());
		for (int i = 0; i < maxFlags; i++) {
			FlagGuess guess = flagGuesses.isEmpty() ? null : flagGuesses.get(i);
			FlagGuess otherGuess = other.flagGuesses.isEmpty() ? null : other.flagGuesses.get(i);
			body.add(Arrays.asList(
				guess != null ? guess.points().toString() : "N/A",
				guess != null ? String.valueOf(guess.guessed()) : "N/A",
				guess != null ? String.valueOf(guess.actual()) : otherGuess != null ? String.valueOf(otherGuess.actual()) : "N/A",
				guess != null ? db.translateFlagName(guess.flag()) : otherGuess != null ? db.translateFlagName(otherGuess.flag()) : "N/A",
				otherGuess != null ? String.valueOf(otherGuess.guessed()) : "N/A",
				otherGuess != null ? otherGuess.points().toString() : "N/A"
			));
		}
		Collections.sort(body, (a, b) -> a.get(3).compareTo(b.get(3)));
		return new Table(translation, header, body);
	}
	
	private Table getWinnerTableComparison(UserScore other) {
		return getPlaceGuessTableComparison(winnerCat, getPlaceGuessMap(winnerGuesses),
			getPlaceGuessMap(other.winnerGuesses));
	}
	
	private Table getTenthTableComparison(UserScore other) {
		return getPlaceGuessTableComparison(tenthCat, getPlaceGuessMap(tenthGuesses),
			getPlaceGuessMap(other.tenthGuesses));
	}

	private <T> Table getStandingsTableComparison(List<StandingsGuess<T>> guesses, List<StandingsGuess<T>> otherGuesses,
			List<String> header, Category category) {
		List<List<String>> body = new ArrayList<>();
		for (int i = 0; i < guesses.size(); i++) {
			StandingsGuess<T> guess = guesses.get(i);
			StandingsGuess<T> otherGuess = otherGuesses.get(i);
			body.add(Arrays.asList(
				guess.points().toString(),
				guess.guessed() != null ? guess.guessed().toString() : "N/A",
				String.valueOf(guess.pos()),
				guess.competitor().toString(),
				otherGuess.guessed() != null ? otherGuess.guessed().toString() : "N/A",
				otherGuess.points().toString()
			));
		}
		String translation = db.translateCategory(category);
		return new Table(translation, header, body);
	}

	private Map<Integer, PlaceGuess> getPlaceGuessMap(List<PlaceGuess> placeGuess) {
		Map<Integer, PlaceGuess> map = new HashMap<>();
		for (PlaceGuess guess : placeGuess) {
			map.put(guess.racePos, guess);
		}
		return map;
	}

	private Table getPlaceGuessTableComparison(Category category, Map<Integer, PlaceGuess> guesses, Map<Integer, PlaceGuess> otherGuesses) {
		List<String> header = Arrays.asList("Poeng", "Plass", "Startet", "Gjettet", "Løp", "Gjettet", "Startet", "Plass", "Poeng");
		String translation = db.translateCategory(category);
		List<List<String>> body = new ArrayList<>();
		for (int i = 1; i < racePos + 1; i++) {
			PlaceGuess guess = guesses.get(i);
			PlaceGuess otherGuess = otherGuesses.get(i);
			if (guess == null && otherGuess == null) {
				continue;
			}
			body.add(Arrays.asList(
				guess != null ? guess.points().toString() : "N/A",
				guess != null ? String.valueOf(guess.finishPos()) : "N/A",
				guess != null ? String.valueOf(guess.startPos()) : "N/A",
				guess != null ? guess.driver() : "N/A",
				guess != null ? guess.raceName() : otherGuess.raceName(),
				otherGuess != null ? otherGuess.driver() : "N/A",
				otherGuess != null ? String.valueOf(otherGuess.startPos()) : "N/A",
				otherGuess != null ? String.valueOf(otherGuess.finishPos()) : "N/A",
				otherGuess != null ? otherGuess.points().toString() : "N/A"

			));
		}
		return new Table(translation, header, body);
	}

	private record StandingsGuess<T>(int pos, T competitor, Integer guessed, Diff diff, Points points) {}
	private record FlagGuess(Flag flag, int guessed, int actual, Diff diff, Points points) {}
	private record PlaceGuess(int racePos, String raceName, String driver, int startPos, int finishPos, Diff diff, Points points) {}
	private class Summary {
		public Points drivers = new Points();
		public Points constructors = new Points();
		public Points flag = new Points();
		public Points winner = new Points();
		public Points tenth = new Points();

		public Points getTotal() {
			return drivers.add(constructors).add(flag).add(winner).add(tenth);
		}
	}
}
