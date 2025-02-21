package no.vebb.f1.controller.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import no.vebb.f1.database.Database;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserMail;
import no.vebb.f1.user.UserMailService;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Username;
import no.vebb.f1.util.exception.InvalidEmailException;
import no.vebb.f1.util.exception.InvalidUsernameException;

/**
 * Class is responsible for managing the user settings. Like changing username
 * and deleting user.
 */
@Controller
@RequestMapping("/settings")
public class UserSettingsController {

	private static final Logger logger = LoggerFactory.getLogger(UserSettingsController.class);

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@Autowired
	private UserMailService userMailService;

	private final String usernameUrl = "/settings/username";

	/**
	 * Handles GET requests for /settings. Gives a list of links to further navigate
	 * the settings.
	 */
	@GetMapping
	public String settings(Model model) {
		model.addAttribute("title", "Innstillinger");
		Map<String, String> linkMap = new LinkedHashMap<>();
		linkMap.put("Se brukerinformasjon", "/settings/info");
		linkMap.put("Påminnelser", "/settings/mail");
		linkMap.put("Endre brukernavn", "/settings/username");
		linkMap.put("Slett bruker", "/settings/delete");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	/**
	 * Handles GET requests for /settings/info. Gives the user information about
	 * their username, user ID and Google ID that is associated with their user.
	 */
	@GetMapping("/info")
	public String userInformation(Model model) {
		model.addAttribute("title", "Brukerinformasjon");
		List<Table> tables = new ArrayList<>();
		User user = userService.loadUser().get();
		tables.add(new Table("", Arrays.asList("Brukernavn"), Arrays.asList(Arrays.asList(user.username))));
		tables.add(new Table("", Arrays.asList("Bruker-ID"), Arrays.asList(Arrays.asList(user.id.toString()))));
		tables.add(new Table("", Arrays.asList("Google-ID"), Arrays.asList(Arrays.asList(user.googleId.toString()))));
		try {
			tables.add(new Table("", Arrays.asList("E-post"), Arrays.asList(Arrays.asList(db.getEmail(user.id)))));
		} catch (EmptyResultDataAccessException e) {
		}
		tables.add(new Table("Tippet sjåfør", Arrays.asList("Plass", "Sjåfør", "År"), db.userGuessDataDriver(user.id)));
		tables.add(new Table("Tippet konstruktør", Arrays.asList("Plass", "Konstruktør", "År"), db.userGuessDataConstructor(user.id)));
		tables.add(new Table("Tippet antall", Arrays.asList("Type", "Tippet", "År"), db.userGuessDataFlag(user.id)));
		tables.add(new Table("Tippet løp", Arrays.asList("Type", "Tippet", "Løp", "År"), db.userGuessDataDriverPlace(user.id)));
		tables.add(new Table("Påminnelser e-post", Arrays.asList("Løp", "År"), db.userDataNotified(user.id)));
		model.addAttribute("tables", tables);
		return "tables";
	}

	/**
	 * Handles GET requests for /settings/username. Gives the form for changing
	 * username.
	 */
	@GetMapping("/username")
	public String changeUsername(Model model) {
		model.addAttribute("url", usernameUrl);
		return "registerUsername";
	}

	/**
	 * Handles POST requests for /settings/username. If the username is valid, it
	 * changes the username in the database. Otherwise, it gives an error message to
	 * the user.
	 */
	@PostMapping("/username")
	public String changeUsername(String username, Model model) {
		try {
			Username validUsername = new Username(username, db);
			final UUID id = userService.loadUser().get().id;
			db.updateUsername(validUsername, id);
		} catch (InvalidUsernameException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("url", usernameUrl);
			return "registerUsername";
		}

		return "redirect:/settings";
	}

	/**
	 * Handles GET requests for /settings/delete. Gives a form to confirm deletion
	 * of account.
	 */
	@GetMapping("/delete")
	public String deleteAccount(Model model) {
		String username = userService.loadUser().get().username;
		model.addAttribute("username", username);

		return "deleteAccount";
	}

	/**
	 * Handles POST requests for /settings/delete. If the input username matches the
	 * username of the user the user is anonymized and Google ID removed. This
	 * revokes their access to the website. If the username is incorrect, the user
	 * gets an error message.
	 */
	@PostMapping("/delete")
	public String deleteAccount(Model model, @RequestParam("username") String username, HttpServletRequest request) {
		User user = userService.loadUser().get();
		String actualUsername = user.username;
		if (!username.equals(actualUsername)) {
			model.addAttribute("username", actualUsername);
			model.addAttribute("error", "Brukernavn er feil");
			return "deleteAccount";
		}
		db.deleteUser(user.id);

		request.getSession().invalidate();
		SecurityContextHolder.clearContext();

		return "redirect:/";
	}

	@GetMapping("/mail")
	public String mailingList(Model model) {
		User user = userService.loadUser().get();
		model.addAttribute("hasMail", db.userHasEmail(user.id));
		return "mail";
	}

	@PostMapping("/mail/add")
	public String addMailingList(Model model, @RequestParam("email") String email) {
		try {
			User user = userService.loadUser().get();
			UserMail userMail = new UserMail(user, email);
			userMailService.sendVerificationCode(userMail);
			return "redirect:/settings/mail/verification";
		} catch (InvalidEmailException e) {
			return "redirect:/settings/mail";
		}
	}
	
	@PostMapping("/mail/remove")
	public String removeMailingList(Model model) {
		User user = userService.loadUser().get();
		db.removeFromMailingList(user.id);
		return "redirect:/settings/mail";
	}

	@GetMapping("/mail/verification")
	public String verificationCodeForm() {
		User user = userService.loadUser().get();
		if (!db.hasVerificationCode(user.id)) {
			return "redirect:/settings/mail";
		}
		return "verificationCode";
	}

	@PostMapping("/mail/verification")
	public String verificationCode(Model model, @RequestParam("code") int code, HttpServletRequest request) {
		User user = userService.loadUser().get();
		boolean isValidVerificationCode = db.isValidVerificationCode(user.id, code);
		if (isValidVerificationCode) {
			logger.info("Successfully verified email of user '{}'", user.id);
			return "redirect:/settings/mail";
		}
		logger.warn("User '{}' put the wrong verification code", user.id);
		
		return "redirect:/settings/mail/verification";
	}

}
