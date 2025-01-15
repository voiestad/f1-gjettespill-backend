package no.vebb.f1.scoring;

import java.util.Map;

import no.vebb.f1.database.Database;

public class DiffPointsMap {
	  
	private Map<Integer, Integer> map;

	public DiffPointsMap(String category, int year, Database db) {
		map = db.getDiffMap(category, year);
	}

	public int getPoints(int diff) {
		Integer points = map.get(diff);
		if (points == null) {
			return 0;
		}
		return points;
	}
}
