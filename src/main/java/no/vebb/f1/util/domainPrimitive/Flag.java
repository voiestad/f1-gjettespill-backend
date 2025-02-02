package no.vebb.f1.util.domainPrimitive;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidFlagException;

public class Flag {
	
	public final String value;
	private Database db;

	public Flag(String value, Database db) throws InvalidFlagException {
		this.value = value;
		this.db = db;
		validate();
	}

	private void validate() {
		if (!db.isValidFlag(value)) {
			throw new InvalidFlagException("Flag : " + value + " is not a valid flag");
		}
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Flag other = (Flag) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
