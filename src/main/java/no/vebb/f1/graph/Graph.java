package no.vebb.f1.graph;

import java.util.List;

import no.vebb.f1.year.YearService;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;
import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.util.domainPrimitive.Year;

@Component
public class Graph {

	private final Database db;
	private final CutoffService cutoffService;
	private final YearService yearService;

	public Graph(Database db, CutoffService cutoffService, YearService yearService) {
		this.db = db;
		this.cutoffService = cutoffService;
		this.yearService = yearService;
	}

	public List<GuesserPointsSeason> getGraph() {
		if (cutoffService.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		return db.getGraph(year);
	}

	public List<RankedGuesser> getleaderboard() {
		if (cutoffService.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		return db.getLeaderboard(year);
	}
}
