package no.vebb.f1.scoring;

import java.util.Map;

import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.year.Year;

public class DiffPointsMap {
	  
	private final Map<Diff, Points> map;

	public DiffPointsMap(Category category, Year year, ScoreService scoreService) {
		map = scoreService.getDiffPointsMap(year, category);
	}

	public Points getPoints(Diff diff) {
		Points points = map.get(diff);
		if (points == null) {
			return new Points(0);
		}
		return points;
	}
}
