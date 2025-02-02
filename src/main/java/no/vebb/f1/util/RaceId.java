package no.vebb.f1.util;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidRaceException;

public class RaceId {
	public final int value;
	private Database db;

	public RaceId(int value, Database db) throws InvalidRaceException {
		this.value = value;
		this.db = db;
		validate();
	}

	private void validate() throws InvalidRaceException {
		if (!db.isRaceAdded(value)) {
			throw new InvalidRaceException("RaceId : " + value + " is not a race ID");
		}
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
