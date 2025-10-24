package no.voiestad.f1.competitors.domain;

import java.util.Objects;
import java.util.Optional;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.lang.NonNull;
import com.fasterxml.jackson.annotation.JsonValue;

@Embeddable
public class Color {
	@Column(name = "color", nullable = false)
	private String value;

	protected Color() {}

	private Color(String value) {
		this.value = value;
	}

	public static Optional<Color> getColor(String value) {
		Color color = new Color(value);
		if (color.isValid()) {
			return Optional.of(color);
		}
		return Optional.empty();
	}

	private boolean isValid() {
		return value != null && value.matches("^#[0-9A-Fa-f]{6}$");
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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Color color)) return false;
        return Objects.equals(value, color.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
