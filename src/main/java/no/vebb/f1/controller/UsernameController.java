package no.vebb.f1.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/username")
public class UsernameController {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
    private UserService userService;

    public UsernameController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


	@GetMapping
	public String registerUsernameForm() {
		Optional<User> user = userService.loadUser();
		if (user.isPresent()) {
			return "redirect:/";
		}
		return "registerUsername";
	}

	@PostMapping
    public String registerUsername(@AuthenticationPrincipal OAuth2User principal, 
                                   @RequestParam("username") String username, 
                                   Model model) {
        final String id = principal.getName();
        
        final String sqlCheckUsername = "SELECT COUNT(*) FROM User WHERE username = ?";
        final Integer usernameCount = jdbcTemplate.queryForObject(sqlCheckUsername, Integer.class, username);

		username = username.strip();

		if (username.equals("")) {
			model.addAttribute("error", "Username cannot be blank.");
			return "registerUsername";
		}

		if (!username.matches("^[a-zA-ZÆØÅæøå ]+$")) {
			model.addAttribute("error", "Username must contain only letters (a-å, A-Å).");
			return "registerUsername";
		}
        
        if (usernameCount != null && usernameCount > 0) {
            model.addAttribute("error", "This username is already taken. Please choose another.");
            return "registerUsername";
        }

        final String sqlInsertUsername = "INSERT INTO User (id, username) VALUES (?, ?)";
        jdbcTemplate.update(sqlInsertUsername, id, username);
        
        return "redirect:/";
    }
}
