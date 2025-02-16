package no.vebb.f1.user;

import no.vebb.f1.util.exception.InvalidEmailException;

public class UserMail {

	public final User user;
	public final String email;

	public UserMail(User user, String email) throws InvalidEmailException {
		this.user = user;
		this.email = email;
		validate();
	}

	private void validate() {
		if (!isValidEmail()) {
			throw new InvalidEmailException("User '" + user.id + "' inputted invalid email");
		}
	}

	private boolean isValidEmail() {
		// TODO: Implement
		return true;
	}

}
