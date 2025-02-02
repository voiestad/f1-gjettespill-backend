package no.vebb.f1.scoring;

import java.util.Map;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.Category;
import no.vebb.f1.util.Year;

public class DiffPointsMap {
	  
	private Map<Integer, Integer> map;

	public DiffPointsMap(Category category, Year year, Database db) {
		map = db.getDiffPointsMap(year, category);
	}

	public int getPoints(int diff) {
		Integer points = map.get(diff);
		if (points == null) {
			return 0;
		}
		return points;
	}
}
