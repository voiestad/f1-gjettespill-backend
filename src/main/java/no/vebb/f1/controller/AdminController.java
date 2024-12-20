package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import no.vebb.f1.importing.Importer;
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
	public String adminHome(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Admin Portal");
		Map<String, String> linkMap = new LinkedHashMap<>();
		linkMap.put("Registrer flagg", "/admin/flag");
		linkMap.put("Administrer sesonger", "/admin/season");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/season")
	public String seasonAdministrate(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Administrer sesonger");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT DISTINCT year FROM Race ORDER BY year DESC";
		List<Integer> years = jdbcTemplate.query(sql, (rs, rowNum) -> Integer.parseInt(rs.getString("year")));
		for (Integer year : years) {
			linkMap.put(String.valueOf(year), "/admin/season/" + year);
		}
		linkMap.put("Legg til ny sesong", "/admin/season/add");
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/season/add")
	public String addSeasonForm() {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		return "addSeason";
	}

	@PostMapping("/season/add")
	public String addSeason(@RequestParam("year") int year, @RequestParam("start") int start,
			@RequestParam("end") int end, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String getRaceNameSql = "SELECT COUNT(*) FROM Race WHERE year = ?";
		boolean isAlreadyAdded = jdbcTemplate.queryForObject(getRaceNameSql, Integer.class, year) > 0;
		if (isAlreadyAdded) {
			model.addAttribute("error", String.format("Sesongen %d er allerede lagt til", year));
			return "addSeason";
		}
		if (start > end) {
			model.addAttribute("error", "Starten av året kan ikke være etter slutten av året");
			return "addSeason";
		}

		List<Integer> races = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			races.add(i);
		}

		Importer importer = new Importer(jdbcTemplate);
		importer.importRaceNames(races, year);
		importer.importData();
		return "redirect:/admin/season";
	}

	@GetMapping("/flag")
	public String flagChooseYear(Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Velg år");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT DISTINCT year FROM Race ORDER BY year DESC";
		List<Integer> years = jdbcTemplate.query(sql, (rs, rowNum) -> Integer.parseInt(rs.getString("year")));
		for (Integer year : years) {
			linkMap.put(String.valueOf(year), "/admin/flag/" + year);
		}
		model.addAttribute("linkMap", linkMap);
		return "linkList";
	}

	@GetMapping("/flag/{year}")
	public String flagChooseRace(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		model.addAttribute("title", "Velg løp");
		Map<String, String> linkMap = new LinkedHashMap<>();
		final String sql = "SELECT name, id FROM Race WHERE year = ? ORDER BY id ASC";
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

	@GetMapping("/flag/{year}/{id}")
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

	@PostMapping("/flag/add")
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

	@PostMapping("/flag/delete")
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

	class RegisteredFlag {
		public final String type;
		public final int round;
		public final int id;

		public RegisteredFlag(String type, int round, int id) {
			this.type = type;
			this.round = round;
			this.id = id;
		}
	}

}
