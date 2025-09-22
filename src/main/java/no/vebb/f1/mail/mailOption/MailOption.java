package no.vebb.f1.mail.mailOption;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class MailOption {
	@Column(name = "mail_option")
	private int value;

	protected MailOption() {}
	public MailOption(int value) {
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
		if (!(o instanceof MailOption that)) return false;
        return value == that.value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
