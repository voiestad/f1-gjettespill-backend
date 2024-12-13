package no.vebb.f1.scoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import no.vebb.f1.user.User;

public class UserScore {
	
	private final User user;
	private final int year;
	private final JdbcTemplate jdbcTemplate;
	private int score;
	private int raceNumber = 1252;
	private final List<List<String>> driversTable = new ArrayList<>();
	private final List<List<String>> constructorsTable = new ArrayList<>();
	private final List<List<String>> flagsTable = new ArrayList<>();
	private final List<List<String>> winnerTable = new ArrayList<>();
	private final List<List<String>> tenthTable = new ArrayList<>();
	private final List<List<String>> summaryTable = new ArrayList<>();

	public UserScore(User user, int year, JdbcTemplate jdbcTemplate) {
		this.user = user;
		this.year = year;
		this.jdbcTemplate = jdbcTemplate;
		summaryTable.add(Arrays.asList("Kategori", "Poeng"));
		initializeDriversTable();
		initializeConstructorsTable();
		initializeFlagsTable();
		initializeWinnerTable();
		initializeTenthTable();
		initializeSummaryTable();
		initializeDummy();
	}

	private void initializeDummy() {
		// Adding dummy values for flagsTable
		List<String> flagsRow = new ArrayList<>();
		flagsRow.add("Flag1");
		flagsRow.add("Flag2");
		flagsTable.add(flagsRow);
		flagsTable.add(flagsRow);
		flagsTable.add(flagsRow);
		flagsTable.add(flagsRow);

		// Adding dummy values for winnerTable
		List<String> winnerRow = new ArrayList<>();
		winnerRow.add("Winner1");
		winnerRow.add("Winner2");
		winnerTable.add(winnerRow);
		winnerTable.add(winnerRow);
		winnerTable.add(winnerRow);
		winnerTable.add(winnerRow);

		// Adding dummy values for tenthTable
		List<String> tenthRow = new ArrayList<>();
		tenthRow.add("Tenth1");
		tenthRow.add("Tenth2");
		tenthTable.add(tenthRow);
		tenthTable.add(tenthRow);
		tenthTable.add(tenthRow);
		tenthTable.add(tenthRow);
	}

	private void initializeDriversTable() {
		DiffPointsMap map = new DiffPointsMap("DRIVER", jdbcTemplate);
		driversTable.add(Arrays.asList("Plass", "Sjåfør", "Gjettet", "Differanse", "Poeng"));
		final String driverStandingsSql = "SELECT driver FROM DriverStandings WHERE race_number = ? ORDER BY position ASC";
		final String guessedSql = "SELECT driver FROM DriverGuess WHERE year = ?  AND guess = ? ORDER BY position ASC";

		int driversScore = getGuessedToPos(map, driverStandingsSql, guessedSql, "driver", driversTable);
		summaryTable.add(Arrays.asList("Sjåfører", String.valueOf(driversScore)));
	}

	private void initializeConstructorsTable() {
		DiffPointsMap map = new DiffPointsMap("CONSTRUCTOR", jdbcTemplate);
		constructorsTable.add(Arrays.asList("Plass", "Konstruktør", "Gjettet", "Differanse", "Poeng"));
		final String constructorStandingsSql = "SELECT constructor FROM ConstructorStandings WHERE race_number = ? ORDER BY position ASC";
		final String guessedSql = "SELECT constructor FROM ConstructorGuess WHERE year = ? AND guesser = ? ORDER BY position ASC";

		int driversScore = getGuessedToPos(map, constructorStandingsSql, guessedSql, "constructor", constructorsTable);
		summaryTable.add(Arrays.asList("Konstruktører", String.valueOf(driversScore)));
	}

	private int getGuessedToPos(DiffPointsMap map, final String driverStandingsSql, final String guessedSql, String colname, List<List<String>> table) {
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
				row.add("0");
			} else {
				int diff = Math.abs(actualPos - pos); 
				int points = map.getPoints(diff);
				competitorScore += points;
				row.add(pos.toString());
				row.add(String.valueOf(diff));
				row.add(String.valueOf(points));
				
			}
			table.add(row);
		}
		score += competitorScore;
		return competitorScore;
	}

	private void initializeFlagsTable() {
		// TODO: Implement this method
	}

	private void initializeWinnerTable() {
		// TODO: Implement this method
	}

	private void initializeTenthTable() {
		// TODO: Implement this method
	}
	
	private void initializeSummaryTable() {
		// TODO: Implement this method
	}

	public int getScore() {
		return score;
	}

	public List<List<String>> getDriversTable() {
		return copy(driversTable);
	}

	public List<List<String>> getConstructorsTable() {
		return copy(constructorsTable);
	}

	public List<List<String>> getFlagsTable() {
		return copy(flagsTable);
	}

	public List<List<String>> getWinnerTable() {
		return copy(winnerTable);
	}

	public List<List<String>> getTenthTable() {
		return copy(tenthTable);
	}

	public List<List<String>> getSummaryTable() {
		return copy(summaryTable);
	}

	private List<List<String>> copy(List<List<String>> original) {
		List<List<String>> copy = new ArrayList<>();
		for (List<String> sublist : original) {
			copy.add(new ArrayList<>(sublist));
		}
		return copy;
	}
}
