package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.util.exception.InvalidPointsException;

public class Points implements Comparable<Points> {
	
	public final int value;

	public Points(int value) throws InvalidPointsException {
		this.value = value;
		validate();
	}

	public Points() {
		this.value = 0;
	}

	private void validate() throws InvalidPointsException {
		if (value < 0) {
			throw new InvalidPointsException("Points can't be negative");
		}
	}

	public Points add(Points other) {
		return new Points(this.value + other.value);
	}

	@JsonValue
    public int toValue() {
        return value;
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
		Points other = (Points) obj;
        return value == other.value;
    }

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(Points o) {
		return Integer.compare(this.value, o.value);	
	}
}
