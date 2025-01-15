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

@Controller
@RequestMapping("/username")
public class UsernameController {

	private static final Logger logger = LoggerFactory.getLogger(UsernameController.class);

	@Autowired
	private Database db;

	private final String url = "/username";

	@Autowired
	private UserService userService;

	@GetMapping
	public String registerUsernameForm(Model model) {
		if (userService.isLoggedIn()) {
			return "redirect:/";
		}
		model.addAttribute("url", url);
		return "registerUsername";
	}

	@PostMapping
	public String registerUsername(@AuthenticationPrincipal OAuth2User principal,
			@RequestParam("username") String username,
			Model model) {
		final String googleId = principal.getName();

		username = username.strip();

		String error = UserUtil.validateUsername(username, db);

		if (error != null) {
			model.addAttribute("error", error);
			model.addAttribute("url", url);
			return "registerUsername";
		}

		try {
			db.addUser(username, googleId);
		} catch (DataAccessException e) {
			// Try again to ensure it could not be equal UUID
			logger.warn("Failed to set UUID to new user. Tried again.");
			db.addUser(username, googleId);
		}
		return "redirect:/";
	}

}
