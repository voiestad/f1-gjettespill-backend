package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.util.exception.InvalidCategoryException;

public class Category {
	
	public final String value;
	private GuessService guessService;

	public Category(String value) {
		this.value = value;
	}

	public Category(String value, GuessService guessService) throws InvalidCategoryException {
		this.value = value;
		this.guessService = guessService;
		validate();
	}

	private void validate() throws InvalidCategoryException {
		if (!guessService.isValidCategory(value)) {
			throw new InvalidCategoryException("Category : " + value + " is not a valid category");
		}
	}

	@Override
	public String toString() {
		return value;
	}

	@JsonValue
    public String toValue() {
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
		Category other = (Category) obj;
		if (value == null) {
            return other.value == null;
		} else return value.equals(other.value);
    }

	
}
