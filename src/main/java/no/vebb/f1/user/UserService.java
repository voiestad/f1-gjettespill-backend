package no.vebb.f1.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;

@Service
public class UserService {

	@Autowired
	private Database db;

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
		if (user.isEmpty()) {
			return false;
		}
		return db.isUserAdmin(user.get().id);
	}

	public boolean isLoggedInUser(User user) {
		Optional<User> optUser = loadUser();
		if (optUser.isEmpty()) {
			return false;
		}
		User loggedInUser = optUser.get();
		return loggedInUser.id.equals(user.id);
	}
	
}
