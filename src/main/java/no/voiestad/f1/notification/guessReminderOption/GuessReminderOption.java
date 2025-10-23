package no.voiestad.f1.notification.guessReminderOption;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GuessReminderOption {
	@Column(name = "guess_reminder_option")
	private int value;

	protected GuessReminderOption() {}
	public GuessReminderOption(int value) {
		this.value = value;
	}

	@JsonValue
	public int value() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof GuessReminderOption that)) return false;
        return value == that.value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
