package no.voiestad.f1.scoring.diffPointsMap;

import java.util.Map;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.scoring.ScoreService;
import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.scoring.domain.Diff;
import no.voiestad.f1.year.Year;

public class DiffPointsMap {
	  
	private final Map<Diff, UserPoints> map;

	public DiffPointsMap(Category category, Year year, ScoreService scoreService) {
		map = scoreService.getDiffPointsMap(year, category);
	}

	public UserPoints getPoints(Diff diff) {
		UserPoints points = map.get(diff);
		if (points == null) {
			return new UserPoints();
		}
		return points;
	}
}
