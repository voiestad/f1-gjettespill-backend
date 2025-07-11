package no.vebb.f1.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.PublicUser;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;

public class UserScore {

	private final Database db;
	public final PublicUser user;
	public final Year year;
	public final RaceId raceId;
	public final int racePos;
	public final List<StandingsGuess<Driver>> driversGuesses = new ArrayList<>();
	public final List<StandingsGuess<Constructor>> constructorsGuesses = new ArrayList<>();
	public final List<FlagGuess> flagGuesses = new ArrayList<>();
	public final List<PlaceGuess> winnerGuesses = new ArrayList<>();
	public final List<PlaceGuess> tenthGuesses = new ArrayList<>();
	public final Summary summary = new Summary();

	public UserScore(PublicUser user, Year year, RaceId raceId, Database db) {
		this.user = user;
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

	public UserScore(PublicUser user, Year year, Database db) {
		this(user, year, UserScore.getRaceId(year, db), db);
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

	private void initializeDriversGuesses() {
		List<Driver> guessedDriver = db.getGuessedYearDriver(year, user.id);
		List<Driver> drivers = db.getDriverStandings(raceId, year);
		Category category = new Category("DRIVER", db);
		summary.drivers = getGuessedToPos(category, guessedDriver, drivers, driversGuesses);
	}

	private void initializeConstructorsGuesses() {
		List<Constructor> guessedConstructor = db.getGuessedYearConstructor(year, user.id);
		List<Constructor> constructors = db.getConstructorStandings(raceId, year);
		Category category = new Category("CONSTRUCTOR", db);
		summary.constructors = getGuessedToPos(category, guessedConstructor, constructors, constructorsGuesses);
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
				result.add(new StandingsGuess<>(actualPos, competitor, null, null, new Points()));
			} else {
				Diff diff = new Diff(Math.abs(actualPos - guessedPos));
				Points points = map.getPoints(diff);
				result.add(new StandingsGuess<>(actualPos, competitor, guessedPos, diff, points));
				competitorScore = competitorScore.add(points);
			}
		}
		return competitorScore;
	}

	private void initializeFlagsGuesses() {
		Category category = new Category("FLAG", db);
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		Points flagScore = new Points();

		List<Map<String, Object>> sqlRes = db.getDataForFlagTable(racePos, year, user.id);
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
		summary.winner = getDriverPlaceGuessTable(new Category("FIRST", db), 1, winnerGuesses);
	}

	private void initializeTenthGuesses() {
		summary.tenth = getDriverPlaceGuessTable(new Category("TENTH", db), 10, tenthGuesses);
	}

	private Points getDriverPlaceGuessTable(Category category, int targetPos, List<PlaceGuess> result) {
		if (raceId == null) {
			return new Points();
		}
		DiffPointsMap map = new DiffPointsMap(category, year, db);
		Points driverPlaceScore = new Points();
		List<Map<String, Object>> sqlRes = db.getDataForPlaceGuessTable(category, user.id, year, racePos);

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

	public record StandingsGuess<T>(int pos, T competitor, Integer guessed, Diff diff, Points points) {}
	public record FlagGuess(Flag flag, int guessed, int actual, Diff diff, Points points) {}
	public record PlaceGuess(int racePos, String raceName, String driver, int startPos, int finishPos, Diff diff, Points points) {}
	public static class Summary {
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
