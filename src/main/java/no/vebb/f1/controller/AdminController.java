package no.vebb.f1.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	public AdminController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping()
	public String adminHome() {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		return "admin";
	}

}
