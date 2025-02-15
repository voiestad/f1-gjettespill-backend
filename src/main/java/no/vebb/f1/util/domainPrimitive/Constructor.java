package no.vebb.f1.util.domainPrimitive;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidConstructorException;

public class Constructor {

	public final String value;
	public final Year year;
	private Database db;

	public Constructor(String value, Database db, Year year) throws InvalidConstructorException {
		this.value = value;
		this.db = db;
		this.year = year;
		validate();
	}

	public Constructor(String value, Database db) throws InvalidConstructorException {
		this(value, db, null);
	}

	public Constructor(String value) {
		this.value = value;
		this.year = null;
	}

	private void validate() throws InvalidConstructorException {
		if (!db.isValidConstructor(this)) {
			throw new InvalidConstructorException("Driver : '" + this + "' is not a valid constructor");
		}
		if (year != null && !db.isValidConstructorYear(this, year)) {
			throw new InvalidConstructorException("Driver : '" + this + "' is not a valid constructor in '" + year + "'");
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
		Constructor other = (Constructor) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
