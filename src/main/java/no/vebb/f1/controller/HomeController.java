package no.vebb.f1.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

import org.springframework.ui.Model;


@Controller
public class HomeController {

	@Autowired
    private UserService userService;

	@GetMapping("/")
	public String home(Model model) {
		Optional<User> user = userService.loadUser();
		boolean loggedIn = user.isPresent();
		model.addAttribute("loggedIn", loggedIn);
		return "public";
	}
}
