package no.vebb.f1.scoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

public class DiffPointsMap {
	  
	private Map<Integer, Integer> map;

	public DiffPointsMap(String category, JdbcTemplate jdbcTemplate, int year) {
		final String sql = "SELECT diff, points FROM DiffPointsMap WHERE category = ? and year = ?";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, category, year);
		map = new HashMap<>();
		for (Map<String, Object> entry : result) {
			Integer diff = (Integer) entry.get("diff");
			Integer points = (Integer) entry.get("points");
			map.put(diff, points);
		}
	}

	public int getPoints(int diff) {
		Integer points = map.get(diff);
		if (points == null) {
			return 0;
		}
		return points;
	}
}
