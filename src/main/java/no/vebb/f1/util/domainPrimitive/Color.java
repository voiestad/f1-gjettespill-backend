package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.util.exception.InvalidColorException;

public class Color {
	
	public final String value;

	public Color(String value) throws InvalidColorException {
		this.value = value;
		validate();
	}

	private void validate() throws InvalidColorException {
		if (value == null) {
			return;
		}
		if (!value.matches("^#[0-9A-Fa-f]{6}$")) {
			throw new InvalidColorException(value + " is not a valid color");
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
		Color other = (Color) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
