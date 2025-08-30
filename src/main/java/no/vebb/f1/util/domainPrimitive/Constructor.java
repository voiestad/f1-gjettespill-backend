package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.competitors.CompetitorService;
import no.vebb.f1.util.exception.InvalidConstructorException;

public class Constructor {

	public final String value;
	public final Year year;
	private CompetitorService competitorService;

	public Constructor(String value, CompetitorService competitorService, Year year) throws InvalidConstructorException {
		this.value = value;
		this.competitorService = competitorService;
		this.year = year;
		validate();
	}

	public Constructor(String value, CompetitorService competitorService) throws InvalidConstructorException {
		this(value, competitorService, null);
	}

	public Constructor(String value) {
		this.value = value;
		this.year = null;
	}

	private void validate() throws InvalidConstructorException {
		if (!competitorService.isValidConstructor(this)) {
			throw new InvalidConstructorException("Driver : '" + this + "' is not a valid constructor");
		}
		if (year != null && !competitorService.isValidConstructorYear(this, year)) {
			throw new InvalidConstructorException("Driver : '" + this + "' is not a valid constructor in '" + year + "'");
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
		Constructor other = (Constructor) obj;
		if (value == null) {
            return other.value == null;
		} else return value.equals(other.value);
    }
}
