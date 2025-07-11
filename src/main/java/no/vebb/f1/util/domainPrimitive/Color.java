package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.util.exception.InvalidColorException;
import org.springframework.lang.NonNull;

public record Color(String value) {

	/**
	 * @throws InvalidColorException if color not in #00ffee format
	 */
	public Color(String value) {
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
	@NonNull
	public String toString() {
		return value;
	}

}
