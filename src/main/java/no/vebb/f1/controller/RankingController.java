package no.vebb.f1.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/guess")
public class RankingController {

	private JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(RankingController.class);

	public RankingController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping()
	public String guess(Model model) {
		return "guessMenu";
	}
	
	@GetMapping("/rank")
	public String rankDrivers(Model model) {
		String sql = "SELECT name FROM Driver";
		List<String> drivers = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"));
		model.addAttribute("items", drivers);
		return "ranking";
	}

	@PostMapping("/rank")
	public String rankDrivers(@RequestParam List<String> rankedItems, Model model) {
		String sql = "SELECT name FROM Driver";
		Set<String> driversCheck = new HashSet<>((jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"))));
		Set<String> guessedDriver = new HashSet<>();
		for (String driver : rankedItems) {
			if (!driversCheck.contains(driver)) {
				throw new IllegalArgumentException("Invalid driver inputted by user");
			}
			if (!guessedDriver.add(driver)) {
				throw new IllegalArgumentException("Duplicate driver inputted by user");
			}
			logger.info("Guessed {}", driver);
		}
		if (driversCheck.size() != guessedDriver.size()) {
			throw new IllegalArgumentException("Not all drivers guessed");
		}
		return "redirect:/guess";
	}
}
