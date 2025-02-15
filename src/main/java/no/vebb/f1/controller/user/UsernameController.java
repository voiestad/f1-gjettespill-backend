package no.vebb.f1.controller.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.domainPrimitive.Username;
import no.vebb.f1.util.exception.InvalidUsernameException;

/**
 * Class is responsible for registering username for new users.
 */
@Controller
@RequestMapping("/username")
public class UsernameController {

	private static final Logger logger = LoggerFactory.getLogger(UsernameController.class);

	@Autowired
	private Database db;

	private final String url = "/username";

	@Autowired
	private UserService userService;

	/**
	 * Handles GET requests for /username. Gives a form to set username.
	 */
	@GetMapping
	public String registerUsernameForm(Model model) {
		if (userService.isLoggedIn()) {
			return "redirect:/";
		}
		model.addAttribute("url", url);
		return "registerUsername";
	}

	/**
	 * Handles POST requests for /username. If the username is valid, it adds the
	 * username along with the users Google ID and a generated ID into the database.
	 * Otherwise, it gives the user an error message.
	 */
	@PostMapping
	public String registerUsername(@AuthenticationPrincipal OAuth2User principal,
			@RequestParam("username") String username,
			Model model) {
		try {
			final String googleId = principal.getName();
			Username validUsername = new Username(username, db);
			try {
				db.addUser(validUsername, googleId);
			} catch (DataAccessException e) {
				// Try again to ensure it could not be equal UUID
				logger.warn("Failed to set UUID to new user. Tried again.");
				db.addUser(validUsername, googleId);
			}
		} catch (InvalidUsernameException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("url", url);
			return "registerUsername";
		}
		return "redirect:/";
	}

}
