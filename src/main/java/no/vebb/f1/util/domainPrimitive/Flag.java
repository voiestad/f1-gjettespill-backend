package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.stats.StatsService;
import no.vebb.f1.util.exception.InvalidFlagException;

public class Flag {
	
	public final String value;
	private StatsService statsService;

	public Flag(String value) {
		this.value = value;
	}

	public Flag(String value, StatsService statsService) throws InvalidFlagException {
		this.value = value;
		this.statsService = statsService;
		validate();
	}

	private void validate() {
		if (!statsService.isValidFlag(value)) {
			throw new InvalidFlagException("Flag : " + value + " is not a valid flag");
		}
	}

	@JsonValue
    public String toValue() {
        return value;
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
            return other.value == null;
		} else return value.equals(other.value);
    }
	
}
