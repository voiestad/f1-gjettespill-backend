package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.year.YearService;

public class Year {

	public final int value;
	private YearService yearService;

	public Year(int value, YearService yearService) throws InvalidYearException {
		this.value = value;
		this.yearService = yearService;
		validate();
	}

	public Year(int value) {
		this.value = value;
	}

	private void validate() throws InvalidYearException {
		if (!yearService.isValidSeason(value)) {
			throw new InvalidYearException("Year : " + value + " is not a valid season");
		}
	}

	@JsonValue
    public int toValue() {
        return value;
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
        return value == other.value;
    }

}
