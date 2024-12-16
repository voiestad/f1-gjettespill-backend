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
		List<String> header = Arrays.asList("Plass", "Sjåfør", "Gjettet", "Differanse", "Poeng");
		final String driverStandingsSql = "SELECT driver FROM DriverStandings WHERE race_number = ? ORDER BY position ASC";
		final String guessedSql = "SELECT driver FROM DriverGuess WHERE year = ?  AND guesser = ? ORDER BY position ASC";

		return getGuessedToPos(map, driverStandingsSql, guessedSql, "driver", header);
	}

	private Table initializeConstructorsTable() {
		DiffPointsMap map = new DiffPointsMap("CONSTRUCTOR", jdbcTemplate, year);
		List<String> header = Arrays.asList("Plass", "Konstruktør", "Gjettet", "Differanse", "Poeng");
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
		final String translationSql = """
				SELECT translation
				FROM CategoryTranslation
				WHERE category = ?
				""";

		String translation = jdbcTemplate.queryForObject(translationSql, String.class, colname.toUpperCase());
		summaryTableBody.add(Arrays.asList(translation, String.valueOf(competitorScore)));
		return new Table(translation, header, body);
	}

	private Table initializeFlagsTable() {
		// TODO: Implement this method
		return new Table("temp", Arrays.asList(), Arrays.asList());
	}
	
	private Table initializeWinnerTable() {
		// TODO: Implement this method
		return new Table("temp", Arrays.asList(), Arrays.asList());
	}
	
	private Table initializeTenthTable() {
		// TODO: Implement this method
		return new Table("temp", Arrays.asList(), Arrays.asList());
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
