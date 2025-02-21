package no.vebb.f1.util.domainPrimitive;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidMailOptionException;

public class MailOption {

	public final int value;
	private Database db;

	public MailOption(int value) {
		this.value = value;
	}
	
	public MailOption(int value, Database db) throws InvalidMailOptionException {
		this.value = value;
		this.db = db;
		validate();
	}

	private void validate() throws InvalidMailOptionException {
		if (!db.isValidMailOption(value)) {
			throw new InvalidMailOptionException("Option: " + value + "is not a valid mail option");
		}
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
