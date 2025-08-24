package no.vebb.f1.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.vebb.f1.util.domainPrimitive.Username;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.exception.NoUsernameException;
import no.vebb.f1.util.exception.NotAdminException;

@Service
public class UserService {

	private final Database db;
	private final UserRespository userRespository;
	private final AdminRepository adminRepository;

	public UserService(Database db, UserRespository userRespository, AdminRepository adminRepository) {
		this.db = db;
		this.userRespository = userRespository;
		this.adminRepository = adminRepository;
	}

	public Optional<User> loadUser() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Optional.empty();
		}
		final String googleId = authentication.getName();
		return userRespository.findByGoogleId(googleId);
	}

	public Optional<User> loadUser(UUID id) {
		return userRespository.findById(id);
	}

	public boolean isLoggedIn() {
		Optional<User> user = loadUser();
		return user.isPresent();
	}

	public boolean isAdmin() {
        return loadUser().map(value -> adminRepository.findById(value.id())).isPresent();
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

	public List<User> getAllUsers() {
		return userRespository.findAllByOrderByUsername();
	}

	public void addUser(Username username, OAuth2User principal) {
		final String googleId = principal.getName();
		User user = new User(UUID.randomUUID(), googleId, username.username);
		userRespository.save(user);
	}

	public void changeUsername(Username newUsername) {
		User user = getUser();
		user.setUsername(newUsername.username);
		userRespository.save(user);
	}

	public void deleteUser() {
		User user = getUser();
		userRespository.anonymizeUser(user.id());
		db.clearUserFromMailing(user.id());
		db.removeBingomaster(user.id());
	}

	public List<UUID> getAdmins() {
		return adminRepository.findAll().stream().map(Admin::id).toList();
	}
}
