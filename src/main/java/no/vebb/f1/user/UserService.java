package no.vebb.f1.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.NoUsernameException;
import no.vebb.f1.util.exception.NotAdminException;

@Service
public class UserService {

	private final Database db;

	public UserService(Database db) {
		this.db = db;
	}

	public Optional<User> loadUser() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Optional.empty();
		}
		final String googleId = authentication.getName();
		try {
			return Optional.of(db.getUserFromGoogleId(googleId));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Optional<User> loadUser(UUID id) {
		try {
			return Optional.of(db.getUserFromId(id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public boolean isLoggedIn() {
		Optional<User> user = loadUser();
		return user.isPresent();
	}

	public boolean isAdmin() {
		Optional<User> user = loadUser();
        return user.filter(value -> db.isUserAdmin(value.id())).isPresent();
    }

	public void adminCheck() throws NotAdminException {
		if (!isAdmin()) {
			throw new NotAdminException("User is not admin and does not have the required permission for this end point");
		}
	}
	
	public void usernameCheck() throws NoUsernameException {
		if (!isLoggedIn()) {
			throw new NoUsernameException("User does not have a username, which is required for this end point");
		}
	}
	public User getUser() throws NoUsernameException {
		Optional<User> user = loadUser();
		if (user.isEmpty()) {
			throw new NoUsernameException("User does not have a username, which is required for this end point");
		}
		return user.get();
	}

	public boolean isBingomaster() {
		Optional<User> user = loadUser();
        return user.filter(value -> db.isBingomaster(value.id())).isPresent();
    }

	public boolean isLoggedInUser(User user) {
		Optional<User> optUser = loadUser();
		if (optUser.isEmpty()) {
			return false;
		}
		User loggedInUser = optUser.get();
		return loggedInUser.id().equals(user.id());
	}

}
