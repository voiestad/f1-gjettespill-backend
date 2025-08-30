package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.util.exception.InvalidDriverException;

public class Driver {
	
	public final String value;
	public final Year year;
	private CompetitorService competitorService;

	public Driver(String value, CompetitorService competitorService, Year year) throws InvalidDriverException {
		this.value = value;
		this.competitorService = competitorService;
		this.year = year;
		validate();
	}

	public Driver(String value, CompetitorService competitorService) throws InvalidDriverException {
		this(value, competitorService, null);
	}

	public Driver(String value) {
		this.value = value;
		this.year = null;
	}

	private void validate() throws InvalidDriverException {
		if (!competitorService.isValidDriver(this)) {
			throw new InvalidDriverException("Driver : '" + this + "' is not a valid driver");
		}
		if (year != null && !competitorService.isValidDriverYear(this, year)) {
			throw new InvalidDriverException("Driver : '" + this + "' is not a valid driver in '" + year + "'");
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
		Driver other = (Driver) obj;
		if (value == null) {
            return other.value == null;
		} else return value.equals(other.value);
    }

	
}
