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

@Component
public class UsernameCheckFilter extends OncePerRequestFilter {

	@Autowired
    private UserService userService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String path = request.getRequestURI();
		if (path.matches(
				"/username|/logout|/favicon.ico|/.*\\.css|/user/*|/score|/score/*|/race-guess|/race-guess/*|/contact")) {
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
