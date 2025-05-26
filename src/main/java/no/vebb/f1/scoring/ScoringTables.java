package no.vebb.f1.scoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;

public class ScoringTables {

	public static Map<Category, Map<Diff, Points>> getScoreMappingTables(Year year, Database db) {
		List<Category> categories = db.getCategories();
		Map<Category, Map<Diff, Points>> result = new HashMap<>();
		for (Category category : categories) {
			result.put(category, db.getDiffPointsMap(year, category)); 
		}
		return result;
	}
}
