package no.vebb.f1.util;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidYearException;

public class Year {

	public final int value;
	private Database db;

	public Year(int value, Database db) throws InvalidYearException {
		this.value = value;
		this.db = db;
		validate();
	}

	private void validate() throws InvalidYearException {
		if (!db.isValidSeason(value)) {
			throw new InvalidYearException("Year : " + value + " is not a valid season");
		}
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
