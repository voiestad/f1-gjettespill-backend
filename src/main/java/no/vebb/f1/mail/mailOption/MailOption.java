package no.vebb.f1.mail.mailOption;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class MailOption {
	@Column(name = "mail_option")
	public int value;

	protected MailOption() {}
	public MailOption(int value) {
		this.value = value;
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
