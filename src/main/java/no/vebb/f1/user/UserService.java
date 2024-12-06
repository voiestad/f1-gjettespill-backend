package no.vebb.f1.user;

import java.util.Optional;

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
		final String id = authentication.getName();
		final String sql = "SELECT username FROM User WHERE id = ?";
		try {
			final String username = jdbcTemplate.queryForObject(sql, String.class, id);
			return Optional.of(new User(id, username));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}
	
}
