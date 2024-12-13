package no.vebb.f1.scoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

public class DiffPointsMap {
	  
	private JdbcTemplate jdbcTemplate;
	private final boolean diffMode;
	private Map<Integer, Integer> map;

	public DiffPointsMap(String category, JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		diffMode = category.equals("FLAGS");
		final String sql = "SELECT diff, points FROM DiffPointsMap WHERE category = ?";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, category);
		map = new HashMap<>();
		for (Map<String, Object> entry : result) {
			Integer diff = (Integer) entry.get("diff");
			Integer points = (Integer) entry.get("points");
			map.put(diff, points);
		}
	}

	public int getPoints(int diff) {
		if (diffMode) {
			return flags(diff);
		}
		Integer points = map.get(diff);
		if (points == null) {
			return 0;
		}
		return points;
	}

	private int flags(int diff) {
		int maxPoint = map.get(0);
		// TODO: implement retrieving number of flags and calculating points
		if (diff == 0) {
			return maxPoint;
		}
		return 0;
	}
}
