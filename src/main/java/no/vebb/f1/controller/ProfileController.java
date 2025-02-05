package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.database.Database;
import no.vebb.f1.scoring.UserScore;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;

/**
 * Class is responsible for displaying stats about users guesses.
 */
@Controller
@RequestMapping("/user")
public class ProfileController {

	@Autowired
	private UserService userService;

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private Database db;

	/**
	 * Handles GET request for /user/{id} where id is the UUID of the guesser.
	 * If user does not exist, redirects to /. If users are still able to guess
	 * before season starts, the page will be blank if the page does not belongs to
	 * the logged in user. Else it will display stats from the users guesses.
	 * 
	 * @param id    of user
	 * @param model
	 * @return guessing profile
	 */
	@GetMapping("/{id}")
	public String guesserProfile(@PathVariable("id") UUID id, Model model) {
		Optional<User> optUser = userService.loadUser(id);
		if (optUser.isEmpty()) {
			return "redirect:/";
		}
		User user = optUser.get();
		return getGuesserProfile(model, user);
	}

	/**
	 * Handles GET request for /user/myprofile. Displays an overview of the
	 * guesses of the user. If the user is not logged in, the user will be
	 * redirected to /.
	 */
	@GetMapping("/myprofile")
	public String myProfile(Model model) {
		Optional<User> optUser = userService.loadUser();
		if (optUser.isEmpty()) {
			return "redirect:/";
		}
		User user = optUser.get();
		return getGuesserProfile(model, user);
	}

	private String getGuesserProfile(Model model, User user) {
		if (!cutoff.isAbleToGuessCurrentYear() || userService.isLoggedInUser(user)) {
			try {
				Year year = new Year(TimeUtil.getCurrentYear(), db);
				UserScore userScore = new UserScore(user.id, year, db);
				model.addAttribute("tables", userScore.getAllTables());
			} catch (InvalidYearException e) {
			}
		} else {
			String title = "Tippingen er tilgjenglig snart!";
			Table table = new Table(title, new ArrayList<>(), new ArrayList<>());
			List<Table> tables = Arrays.asList(table);
			model.addAttribute("tables", tables);
		}

		model.addAttribute("title", user.username);
		model.addAttribute("loggedOut", !userService.isLoggedIn());
		return "profile";
	}

}
