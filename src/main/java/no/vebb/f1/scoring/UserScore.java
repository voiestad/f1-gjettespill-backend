package no.vebb.f1.scoring;

import java.util.ArrayList;
import java.util.List;

import no.vebb.f1.user.User;

public class UserScore {
	
	private final User user;
	private double score;
	private final List<List<String>> driversTable = new ArrayList<>();
	private final List<List<String>> constructorsTable = new ArrayList<>();
	private final List<List<String>> flagsTable = new ArrayList<>();
	private final List<List<String>> winnerTable = new ArrayList<>();
	private final List<List<String>> tenthTable = new ArrayList<>();
	private final List<List<String>> summaryTable = new ArrayList<>();

	public UserScore(User user, int year) {
		this.user = user;
		initializeDriversTable();
		initializeConstructorsTable();
		initializeFlagsTable();
		initializeWinnerTable();
		initializeTenthTable();
		initializeSummaryTable();
		initializeDummy();
	}

	private void initializeDummy() {
		// Adding dummy values for driversTable
		List<String> driversRow = new ArrayList<>();
		driversRow.add("Driver1");
		driversRow.add("Driver2");
		driversTable.add(driversRow);
		driversTable.add(driversRow);
		driversTable.add(driversRow);
		driversTable.add(driversRow);

		// Adding dummy values for constructorsTable
		List<String> constructorsRow = new ArrayList<>();
		constructorsRow.add("Constructor1");
		constructorsRow.add("Constructor2");
		constructorsTable.add(constructorsRow);
		constructorsTable.add(constructorsRow);
		constructorsTable.add(constructorsRow);
		constructorsTable.add(constructorsRow);

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
		
		// Adding dummy values for summaryTable
		List<String> summaryRow = new ArrayList<>();
		summaryRow.add("Summary1");
		summaryRow.add("Summary2");
		summaryTable.add(summaryRow);
		summaryTable.add(summaryRow);
		summaryTable.add(summaryRow);
	}

	private void initializeDriversTable() {
		// TODO: Implement this method
	}

	private void initializeConstructorsTable() {
		// TODO: Implement this method
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

	public double getScore() {
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
