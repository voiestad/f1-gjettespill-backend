package no.vebb.f1.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MailOption {
	@Column(name = "mail_option")
	public int value;

	protected MailOption() {}
	protected MailOption(int value) {
		this.value = value;
	}

}
