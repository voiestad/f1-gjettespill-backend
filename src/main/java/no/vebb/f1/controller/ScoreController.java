package no.vebb.f1.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import no.vebb.f1.util.Table;

@Controller
@RequestMapping("/score")
public class ScoreController {

	private final JdbcTemplate jdbcTemplate;
	private int year = 2024;

	public ScoreController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping
	public String scoreMappingTables(Model model) {
		List<Table> scoreMappingTables = getScoreMappingTables();
		model.addAttribute("scoreMappingTables", scoreMappingTables);
		return "score";
	}

	private List<Table> getScoreMappingTables() {
		List<Table> scoreMappingTables = new ArrayList<>();
		final String sql = """
				SELECT name
				FROM Category
				""";
		List<String> categories = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"));
		for (String category : categories) {
			scoreMappingTables.add(getTable(category));
		}

		return scoreMappingTables;
	}

	private Table getTable(String category) {
		List<String> header = Arrays.asList("Differanse", "Poeng");
		final String sql = """
				SELECT points, diff
				FROM DiffPointsMap
				WHERE year = ? AND category = ?
				ORDER BY diff ASC
				""";

		final String translationSql = """
				SELECT translation
				FROM CategoryTranslation
				WHERE category = ?
				""";

		String translation = jdbcTemplate.queryForObject(translationSql, String.class, category);
		List<List<String>> body = new ArrayList<>();

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, year, category);
		for (Map<String, Object> row : rows) {
			int diff = (int) row.get("diff");
			int points = (int) row.get("points");
			body.add(Arrays.asList(String.valueOf(diff), String.valueOf(points)));
		}
		return new Table(translation, header, body);
	}

}
