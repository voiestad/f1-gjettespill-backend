package no.vebb.f1.util.domainPrimitive;

import no.vebb.f1.mail.MailService;
import no.vebb.f1.util.exception.InvalidMailOptionException;

public class MailOption {

	public final int value;
	private MailService mailService;

	public MailOption(int value) {
		this.value = value;
	}
	
	public MailOption(int value, MailService mailService) throws InvalidMailOptionException {
		this.value = value;
		this.mailService = mailService;
		validate();
	}

	private void validate() throws InvalidMailOptionException {
		if (!mailService.isValidMailOption(value)) {
			throw new InvalidMailOptionException("Option: " + value + "is not a valid mail option");
		}
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
