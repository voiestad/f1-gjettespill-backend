package no.vebb.f1.scoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import no.vebb.f1.user.User;
import no.vebb.f1.util.Table;

public class UserScore {
	
	private final User user;
	private final int year;
	private final JdbcTemplate jdbcTemplate;
	private int score;
	private int raceNumber = 1252;
	public final Table driversTable;
	public final Table constructorsTable;
	public final Table flagsTable;
	public final Table winnerTable;
	public final Table tenthTable;
	public final Table summaryTable;
	private final List<List<String>> summaryTableBody = new ArrayList<>();

	public UserScore(User user, int year, JdbcTemplate jdbcTemplate) {
		this.user = user;
		this.year = year;
		this.jdbcTemplate = jdbcTemplate;
		this.driversTable = initializeDriversTable();
		this.constructorsTable = initializeConstructorsTable();
		this.flagsTable = initializeFlagsTable();
		this.winnerTable = initializeWinnerTable();
		this.tenthTable = initializeTenthTable();
		this.summaryTable = initializeSummaryTable();
	}

	private Table initializeDriversTable() {
		DiffPointsMap map = new DiffPointsMap("DRIVER", jdbcTemplate, year);
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Gjettet", "Diff", "Poeng");
		final String driverStandingsSql = "SELECT driver FROM DriverStandings WHERE race_number = ? ORDER BY position ASC";
		final String guessedSql = "SELECT driver FROM DriverGuess WHERE year = ?  AND guesser = ? ORDER BY position ASC";

		return getGuessedToPos(map, driverStandingsSql, guessedSql, "driver", header);
	}

	private Table initializeConstructorsTable() {
		DiffPointsMap map = new DiffPointsMap("CONSTRUCTOR", jdbcTemplate, year);
		List<String> header = Arrays.asList("Plass", "Konstruktør", "Gjettet", "Diff", "Poeng");
		final String constructorStandingsSql = "SELECT constructor FROM ConstructorStandings WHERE race_number = ? ORDER BY position ASC";
		final String guessedSql = "SELECT constructor FROM ConstructorGuess WHERE year = ? AND guesser = ? ORDER BY position ASC";

		return getGuessedToPos(map, constructorStandingsSql, guessedSql, "constructor", header);
	}

	private Table getGuessedToPos(DiffPointsMap map, final String driverStandingsSql, final String guessedSql, String colname, List<String> header) {
		List<List<String>> body = new ArrayList<>();
		int competitorScore = 0;
		List<String> competitors = jdbcTemplate.query(driverStandingsSql, (rs, rowNum) -> rs.getString(colname), raceNumber);
		List<String> guessed = jdbcTemplate.query(guessedSql, (rs, rowNum) -> rs.getString(colname), year, user.id);
		Map<String, Integer> guessedToPos = new HashMap<>();
		for (int i = 0; i < guessed.size(); i++) {
			guessedToPos.put(guessed.get(i), i+1);
		}
		for (int i = 0; i < competitors.size(); i++) {
			List<String> row = new ArrayList<>();
			int actualPos = i + 1;
			row.add(String.valueOf(actualPos));
			String driver = competitors.get(i);
			row.add(driver);
			Integer pos = guessedToPos.get(driver);
			if (pos == null) {
				row.add("N/A");
				row.add("N/A");
				row.add("0");
			} else {
				int diff = Math.abs(actualPos - pos); 
				int points = map.getPoints(diff);
				competitorScore += points;
				row.add(pos.toString());
				row.add(String.valueOf(diff));
				row.add(String.valueOf(points));
				
			}
			body.add(row);
		}
		score += competitorScore;
		String translation = translateCategory(colname.toUpperCase());
		summaryTableBody.add(Arrays.asList(translation, String.valueOf(competitorScore)));
		return new Table(translation, header, body);
	}

	private Table initializeFlagsTable() {
		// TODO: Implement this method
		return new Table("Antall", Arrays.asList(), Arrays.asList());
	}
	
	private Table initializeWinnerTable() {
		return getDriverPlaceGuessTable("FIRST", 1);
	}
	
	private Table initializeTenthTable() {
		return getDriverPlaceGuessTable("TENTH", 10);
	}

	private Table getDriverPlaceGuessTable(String category, int targetPos) {
		DiffPointsMap map = new DiffPointsMap(category, jdbcTemplate, year);
		List<String> header = Arrays.asList("Løp", "Gjettet", "Startet", "Plass", "Poeng");
		List<List<String>> body = new ArrayList<>();
		int driverPlaceScore = 0;
		final String sql = """
		SELECT r.name AS race_name, dpg.driver AS driver, sg.position AS start, rr.finishing_position AS finish FROM
		DriverPlaceGuess dpg
		JOIN Race r ON r.id = dpg.race_number
		JOIN StartingGrid sg ON sg.race_number = r.id AND dpg.driver = sg.driver
		JOIN RaceResult rr ON rr.race_number = r.id AND dpg.driver = rr.driver
		WHERE dpg.category = ? AND dpg.guesser = ? AND r.year = ?
		ORDER BY r.id ASC
		""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, category, user.id, year);

		for (Map<String, Object> row : sqlRes) {
			String raceName = (String) row.get("race_name");
			String driver = (String) row.get("driver");
			int startPos = (int) row.get("start");
			int finishPos = (int) row.get("finish");
			int diff = Math.abs(targetPos - finishPos);
			int score = map.getPoints(diff);
			driverPlaceScore += score;
			body.add(Arrays.asList(raceName, driver, String.valueOf(startPos), String.valueOf(finishPos), String.valueOf(score))); 
		}
		score += driverPlaceScore;

		String translation = translateCategory(category);
		summaryTableBody.add(Arrays.asList(translation, String.valueOf(driverPlaceScore)));
		return new Table(translation, header, body);
	}

	private String translateCategory(String category) {
		final String translationSql = """
				SELECT translation
				FROM CategoryTranslation
				WHERE category = ?
				""";

		return jdbcTemplate.queryForObject(translationSql, String.class, category);
	}
	
	private Table initializeSummaryTable() {
		List<String> header = Arrays.asList("Kategori", "Poeng");
		return new Table("Oppsummering", header, summaryTableBody);
	}

	public int getScore() {
		return score;
	}

	public List<Table> getAllTables() {
		List<Table> tables = Arrays.asList(summaryTable, driversTable, constructorsTable, flagsTable, winnerTable, tenthTable);
		return tables;
	}
}
