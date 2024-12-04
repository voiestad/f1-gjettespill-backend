package no.vebb.f1.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

	@GetMapping("/")
	public String home() {
		return "Home";
	}

	@GetMapping("/user")
	public String getUser(@AuthenticationPrincipal OAuth2User principal) {
		return principal.getName();
	}

}
