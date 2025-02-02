package no.vebb.f1.util.domainPrimitive;

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

	public Year(int value) {
		this.value = value;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Year other = (Year) obj;
		if (value != other.value)
			return false;
		return true;
	}

}
