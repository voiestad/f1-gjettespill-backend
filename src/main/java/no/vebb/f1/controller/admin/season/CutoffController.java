package no.vebb.f1.controller.admin.season;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import no.vebb.f1.util.CutoffRace;

@Controller
@RequestMapping("/admin/season/{year}/cutoff")
public class CutoffController {

	@Autowired
	private UserService userService;

	private JdbcTemplate jdbcTemplate;

	public CutoffController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping
	public String manageCutoff(@PathVariable("year") int year, Model model) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}

		List<CutoffRace> races = new ArrayList<>();
		final String getCutoffRaces = """
						SELECT r.id as id, r.name as name, rc.cutoff as cutoff, ro.year as year, ro.position as position
						FROM RaceCutoff rc
						JOIN RaceOrder ro ON ro.id = rc.race_number
						JOIN Race r ON ro.id = r.id
						WHERE ro.year = ?
						ORDER BY ro.position ASC
				""";
		List<Map<String, Object>> sqlRes = jdbcTemplate.queryForList(getCutoffRaces, year);
		for (Map<String, Object> row : sqlRes) {
			LocalDateTime cutoff = instantToLocalTime(Instant.parse((String) row.get("cutoff")));
			String name = (String) row.get("name");
			int id = (int) row.get("id");
			int position = (int) row.get("position");
			CutoffRace race = new CutoffRace(position, name, id, cutoff);
			races.add(race);
		}

		final String getCutoffYear = "SELECT cutoff FROM YearCutoff WHERE year = ?";
		String unparsedCutoffYear = jdbcTemplate.queryForObject(getCutoffYear, (rs, rowNum) -> rs.getString("cutoff"),
				year);
		LocalDateTime cutoffYear = instantToLocalTime(Instant.parse(unparsedCutoffYear));
		model.addAttribute("title", year);
		model.addAttribute("races", races);
		model.addAttribute("cutoffYear", cutoffYear);
		return "cutoff";
	}

	@PostMapping("/setRace")
	public String setCutoffRace(@PathVariable("year") int year, @RequestParam("id") int id,
			@RequestParam("cutoff") String cutoff) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		final String validateRaceId = "SELECT COUNT(*) FROM RaceOrder WHERE year = ? AND id = ?";
		boolean isValidRaceId = jdbcTemplate.queryForObject(validateRaceId, Integer.class, year, id) > 0;
		if (!isValidRaceId) {
			return "redirect:/admin/season/" + year + "/cutoff";
		}
		try {
			Instant cutoffTime = parseTimeInput(cutoff);
			final String setCutoffTime = "INSERT OR REPLACE INTO RaceCutoff (race_number, cutoff) VALUES (?, ?)";
			jdbcTemplate.update(setCutoffTime, id, cutoffTime.toString());
		} catch (DateTimeParseException e) {

		}
		return "redirect:/admin/season/" + year + "/cutoff";
	}

	@PostMapping("/setYear")
	public String setCutoffYear(@PathVariable("year") int year, @RequestParam("cutoff") String cutoff) {
		if (!userService.isAdmin()) {
			return "redirect:/";
		}
		final String validateSeason = "SELECT COUNT(*) FROM RaceOrder WHERE year = ?";
		boolean isValidYear = jdbcTemplate.queryForObject(validateSeason, Integer.class, year) > 0;
		if (!isValidYear) {
			return "redirect:/admin/season";
		}
		try {
			Instant cutoffTime = parseTimeInput(cutoff);
			final String setCutoffTime = "INSERT OR REPLACE INTO YearCutoff (year, cutoff) VALUES (?, ?)";
			jdbcTemplate.update(setCutoffTime, year, cutoffTime.toString());
		} catch (DateTimeParseException e) {

		}
		return "redirect:/admin/season/" + year + "/cutoff";
	}

	private Instant parseTimeInput(String inputTime) throws DateTimeParseException {
		LocalDateTime localDateTime = LocalDateTime.parse(inputTime);
		ZoneId zoneId = ZoneId.of("Europe/Paris");
		ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
		return zonedDateTime.toInstant();
	}

	private LocalDateTime instantToLocalTime(Instant inputTime) {
		ZoneId zoneId = ZoneId.of("Europe/Paris");
		ZonedDateTime zonedDateTime = inputTime.atZone(zoneId);
		return zonedDateTime.toLocalDateTime();
	}
}
