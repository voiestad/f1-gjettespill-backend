package no.vebb.f1.controller.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Username;
import no.vebb.f1.util.exception.InvalidUsernameException;

/**
 * Class is responsible for managing the user settings. Like changing username
 * and deleting user.
 */
@Controller
@RequestMapping("/settings")
public class UserSettingsController {

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

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

}
