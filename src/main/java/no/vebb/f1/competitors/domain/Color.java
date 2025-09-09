package no.vebb.f1.competitors.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.util.exception.InvalidColorException;
import org.springframework.lang.NonNull;

@Embeddable
public class Color {
	@Column(name = "color", nullable = false)
	private String value;

	protected Color() {}

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
	@NonNull
	public String toString() {
		return value;
	}

}
