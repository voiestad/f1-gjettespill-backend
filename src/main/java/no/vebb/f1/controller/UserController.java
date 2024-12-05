package no.vebb.f1.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/username")
public class UserController {

	private final JdbcTemplate jdbcTemplate;

    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


	@GetMapping
	public String registerUsernameForm(@AuthenticationPrincipal OAuth2User principal) {
		final String id = principal.getName();
		final String sql = "SELECT COUNT(*) FROM User WHERE id = ?";
		final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
		if (count != null && count > 0) {
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
