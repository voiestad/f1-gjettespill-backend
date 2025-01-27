package no.vebb.f1.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.vebb.f1.user.UserService;

import java.io.IOException;
import java.security.Principal;

/**
 * Class is responsible for redirecting users to the /username page when they
 * are logged in.
 * This is to ensure that a user cannot be logged in and make requests that
 * requires having a
 * username and user ID.
 */
@Component
public class UsernameCheckFilter extends OncePerRequestFilter {

	@Autowired
	private UserService userService;

	/**
	 * Redirects user to /username when user logged in and does not have a username.
	 * Exceptions to redirections are requests to /username, /logout, /favicon and
	 * all .css files.
	 */
	@Override
	@SuppressWarnings("null")
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String path = request.getRequestURI();
		if (path.matches(
				"/username|/logout|/favicon.ico|/.*\\.css")) {
			filterChain.doFilter(request, response);
			return;
		}
		final Principal principal = request.getUserPrincipal();
		if (principal != null) {
			if (!userService.isLoggedIn()) {
				response.sendRedirect("/username");
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
}
