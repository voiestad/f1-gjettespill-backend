package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.util.exception.InvalidDiffException;

public class Diff {
	
	public final int value;

	public Diff() {
		this.value = 0;
	}

	public Diff(int value) throws InvalidDiffException {
		this.value = value;
		validate();
	}

	@JsonValue
    public int toValue() {
        return value;
    }

	private void validate() throws InvalidDiffException {
		if (value < 0) {
			throw new InvalidDiffException("Diff can't be negative.");
		}
	}

	public Diff add(Diff other) {
		return new Diff(this.value + other.value);
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
		Diff other = (Diff) obj;
		if (value != other.value)
			return false;
		return true;
	}
	
}
