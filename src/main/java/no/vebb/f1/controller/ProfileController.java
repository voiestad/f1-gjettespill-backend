package no.vebb.f1.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.scoring.UserScore;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/user")
public class ProfileController {

	private int year = 2024;

	@Autowired
	private UserService userService;

	@GetMapping("/{id}")
	public String guesserProfile(@PathVariable("id") UUID id, Model model) {
		Optional<User> optUser = userService.loadUser(id);
		if (optUser.isEmpty()) {
			return "redirect:/";
		}
		User user = optUser.get();
		UserScore userScore = new UserScore(user, year);

		model.addAttribute("summary", userScore.getSummaryTable());
		model.addAttribute("drivers", userScore.getDriversTable());
		model.addAttribute("constructors", userScore.getConstructorsTable());
		model.addAttribute("flags", userScore.getFlagsTable());
		model.addAttribute("winners", userScore.getWinnerTable());
		model.addAttribute("tenth", userScore.getTenthTable());
		model.addAttribute("title", user.username);
		return "profile";
	}
}
