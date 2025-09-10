package no.vebb.f1.scoring;

import java.util.Map;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.year.Year;

public class DiffPointsMap {
	  
	private final Map<Diff, UserPoints> map;

	public DiffPointsMap(Category category, Year year, ScoreService scoreService) {
		map = scoreService.getDiffPointsMap(year, category);
	}

	public UserPoints getPoints(Diff diff) {
		UserPoints points = map.get(diff);
		if (points == null) {
			return new UserPoints(0);
		}
		return points;
	}
}
