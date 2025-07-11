package no.vebb.f1.user;

import no.vebb.f1.util.exception.InvalidEmailException;

public record UserMail(User user, String email) {

	/**
	 * @throws InvalidEmailException when email invalid
	 */
	public UserMail(User user, String email) {
		this.user = user;
		this.email = email;
		validate();
	}

	private void validate() throws InvalidEmailException {
		if (!isValidEmail()) {
			throw new InvalidEmailException("User '" + user.id() + "' inputted invalid email");
		}
	}

	private boolean isValidEmail() {
		return email.matches("^([\\w\\-.])+(\\+?[\\w\\-.]+)?@([\\w\\-]+\\.)+[\\w\\-]{2,4}$");
	}

}
