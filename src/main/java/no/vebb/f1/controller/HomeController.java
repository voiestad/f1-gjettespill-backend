package no.vebb.f1.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;


@Controller
public class HomeController {

	@GetMapping("/")
	public String home(Model model) {
		boolean loggedIn = false;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
			loggedIn = true;
		}

		model.addAttribute("loggedIn", loggedIn);
		return "public";
	}
}
