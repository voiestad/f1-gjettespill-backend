package no.vebb.f1.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final JdbcTemplate jdbcTemplate;

	public UserService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<User> loadUser() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Optional.empty();
		}
		final String googleId = authentication.getName();
		final String sql = "SELECT username, id FROM User WHERE google_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				String username = rs.getString("username");
				UUID id = UUID.fromString(rs.getString("id"));
				return Optional.of(new User(googleId, id, username));
			}, googleId);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Optional<User> loadUser(UUID id) {
		final String sql = "SELECT username, google_id FROM User WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				String username = rs.getString("username");
				String googleId = rs.getString("google_id");
				return Optional.of(new User(googleId, id, username));
			}, id);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public boolean isLoggedIn() {
		Optional<User> user = loadUser();
		return user.isPresent();
	}
	
}
