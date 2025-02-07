package no.vebb.f1.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

@Controller
public class F1ErrorController implements ErrorController {

	private static final Logger logger = LoggerFactory.getLogger(F1ErrorController.class);

	@Autowired
	private UserService userService;

	/**
	 * Handles GET requests for /error and therefore handles requests that could not
	 * be handled properly elsewhere.
	 */
	@RequestMapping("/error")
	public String error(HttpServletRequest request) {
		Optional<User> user = userService.loadUser();
		String userId = user.map(u -> u.id.toString()).orElse("unknown");
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		String referer = (referer = request.getHeader("Referer")) == null ? "unknown" : referer;
		String url = (url = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)) == null ? "unknown" : url;

		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());
		
			if (statusCode.equals(HttpStatus.NOT_FOUND.value())) {
				logger.error("User '{}' received a 404 error and tried accessing '{}', coming from '{}'.", userId, url, referer);
				return "error-404";
			} else if (statusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
				logger.error("User '{}' received a 500 error while accessing '{}', coming from '{}'.", userId, url, referer);
				return "error-500";
			}
		}
		logger.error("User '{}' received an error unknown error at '{}', coming from '{}'.", userId, url, referer);
		return "error";
	}
}
