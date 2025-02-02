package no.vebb.f1.util.domainPrimitive;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.InvalidCategoryException;

public class Category {
	
	public final String value;
	private Database db;

	public Category(String value, Database db) throws InvalidCategoryException {
		this.value = value;
		this.db = db;
		validate();
	}

	private void validate() throws InvalidCategoryException {
		if (!db.isValidCategory(value)) {
			throw new InvalidCategoryException("Category : " + value + " is not a valid category");
		}
	}

	@Override
	public String toString() {
		return value;
	}
}
