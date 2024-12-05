package no.vebb.f1.components;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@Component
public class UsernameCheckFilter extends OncePerRequestFilter {

	private final JdbcTemplate jdbcTemplate;

	public UsernameCheckFilter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String path = request.getRequestURI();
		if (path.matches("/username|/logout")) {
			filterChain.doFilter(request, response);
			return;
		}
		final Principal principal = request.getUserPrincipal();
		if (principal != null) {
			final String id = principal.getName();
			final String sql = "SELECT COUNT(*) FROM User WHERE id = ?";
			final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
			if (count != null && count > 0) {
				filterChain.doFilter(request, response);
				return;
			}
			response.sendRedirect("/username");
		}
		filterChain.doFilter(request, response);
	}
}
