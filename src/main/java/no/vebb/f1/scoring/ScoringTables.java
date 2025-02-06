package no.vebb.f1.scoring;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.collection.Table;
import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;

public class ScoringTables {

	public static List<Table> getScoreMappingTables(Year year, Database db) {
		List<Category> categories = db.getCategories();
		return categories.stream()
				.map(category -> getTable(category, year, db))
				.toList();
	}

	private static Table getTable(Category category, Year year, Database db) {
		List<String> header = Arrays.asList("Differanse", "Poeng");
		Map<Diff, Points> map = db.getDiffPointsMap(year, category);
		List<List<String>> body = map.entrySet().stream()
				.map(entry -> Arrays.asList(
						String.valueOf(entry.getKey()),
						String.valueOf(entry.getValue())))
				.toList();
		String translation = db.translateCategory(category);
		return new Table(translation, header, body);
	}
}
