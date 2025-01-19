package no.vebb.f1.controller.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.user.UserService;
import no.vebb.f1.util.RegisteredFlag;

@Controller
@RequestMapping("/admin/flag")
public class FlagController {
	
	@Autowired
	private UserService userService;
	
	private JdbcTemplate jdbcTemplate;

	public FlagController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping
	public String flagChooseYear(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Velg år");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT DISTINCT year FROM RaceOrder ORDER BY year DESC";
		List<Integer> years = jdbcTemplate.query(sql, (rs, rowNum) -> Integer.parseInt(rs.getString("year")));
		for (Integer year : years) {
			linkMap.put(String.valueOf(year), "/admin/flag/" + year);
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/{year}")
	public String flagChooseRace(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Velg løp");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT r.name AS name, r.id AS id FROM Race r JOIN RaceOrder ro ON ro.id = r.id WHERE year = ? ORDER BY position ASC";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(sql, year);
		int i = 1;
		for (Map<String, Object> row : sqlRes) {
			String name = (String) row.get("name");
			int id = (int) row.get("id");
			linkMap.put(i++ + ". " + name, "/admin/flag/" + year + "/" + id);
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/{year}/{id}")
	public String registrerFlags(@PathVariable("year") int year, @PathVariable("id") int raceId, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		List<String> flags = getFlags();
		model.addAttribute("flags", flags);
		model.addAttribute("raceId", raceId);

		List<RegisteredFlag> registeredFlags = new ArrayList<>();
		final String getRegisteredFlags = "SELECT flag, round, id FROM FlagStats WHERE race_number = ?";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getRegisteredFlags, raceId);
		for (Map<String, Object> row : sqlRes) {
			String type = (String) row.get("flag");
			int round = (int) row.get("round");
			int id = (int) row.get("id");

			registeredFlags.add(new RegisteredFlag(type, round, id));
		}

		model.addAttribute("registeredFlags", registeredFlags);

		final String getRaceNameSql = "SELECT name FROM Race WHERE id = ?";
		String raceName = jdbcTemplate.queryForObject(getRaceNameSql, (rs, rowNum) -> rs.getString("name"), raceId);
		model.addAttribute("title", "Flagg " + raceName);
		return "noteFlags";
	}

	@PostMapping("/add")
	public String registerFlag(@RequestParam("flag") String flag, @RequestParam("round") int round,
			@RequestParam("raceId") int raceId, @RequestParam("origin") String origin) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		Set<String> flags = new HashSet<>(getFlags());
		if (!flags.contains(flag)) {
			return "redirect:/";
		}

		final String sql = "INSERT INTO FlagStats (flag, race_number, round) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, flag, raceId, round);

		return "redirect:" + origin;
	}

	@PostMapping("/delete")
	public String deleteFlag(@RequestParam("id") int id, @RequestParam("origin") String origin) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}

		final String sql = "DELETE FROM FlagStats WHERE id = ?";
		jdbcTemplate.update(sql, id);

		return "redirect:" + origin;
	}

	private List<String> getFlags() {
		final String sql = "SELECT name FROM Flag";
		return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"));
	}

}
