package no.vebb.f1.importing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImporter {

	private static final Logger logger = LoggerFactory.getLogger(TableImporter.class);

	public static List<List<String>> getTable(String url) {
		List<List<String>> table = new ArrayList<>();
		try {
			Document doc = Jsoup.connect(url).get();
			Element htmlTable = doc.getElementsByTag("table").first();
			if (htmlTable == null) {
				return table;
			}
			for (Element row : htmlTable.getElementsByTag("tr")) {
				List<String> tableRow = new ArrayList<>();
				for (Element entry : row.getElementsByTag("th")) {
					tableRow.add(entry.text());
				}
				for (Element entry : row.getElementsByTag("td")) {
					tableRow.add(entry.text());
				}
				if (tableRow.size() > 1) {
					table.add(tableRow);
				}
			}
		} catch (IOException e) {
			logger.info("Failed to import from: '{}'", url);
		}
		return table;
	}

	public static List<List<String>> getDriverStandings(Year year) {
		String url = String.format("https://www.formula1.com/en/results/%s/drivers", year);
		return TableImporter.getTable(url);
	}
	
	public static List<List<String>> getConstructorStandings(Year year) {
		String url = String.format("https://www.formula1.com/en/results/%s/team", year);
		return TableImporter.getTable(url);
	}

	public static List<List<String>> getStartingGrid(RaceId raceId) {
		String url = String.format("https://www.formula1.com/en/results/a/races/%s/a/starting-grid", raceId);
		return TableImporter.getTable(url);
	}

	public static List<List<String>> getRaceResult(RaceId raceId) {
		String url = String.format("https://www.formula1.com/en/results/a/races/%s/a/race-result", raceId);
		return TableImporter.getTable(url);
	}
	
	public static String getGrandPrixName(int raceId) {
		String url = String.format("https://www.formula1.com/en/results/a/races/%d/a/race-result", raceId);
		String name = "";
		try {
			Document doc = Jsoup.connect(url).ignoreContentType(true).get();
			doc.outputSettings().charset("UTF-8");
			Elements pTags = doc.getElementsByClass("typography-module_body-xs-semibold__Fyfwn");
			if (pTags.isEmpty()) {
				return name;
			}
			name = pTags.get(0).text().split(",")[1].strip();
		} catch (IOException ignored) {
		}
		return name;
	}

}
