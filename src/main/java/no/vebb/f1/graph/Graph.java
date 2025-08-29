package no.vebb.f1.graph;

import java.util.List;

import no.vebb.f1.year.YearService;
import org.springframework.stereotype.Component;

import no.vebb.f1.database.Database;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.util.domainPrimitive.Year;

@Component
public class Graph {

	private final Database db;
	private final Cutoff cutoff;
	private final YearService yearService;

	public Graph(Database db, Cutoff cutoff, YearService yearService) {
		this.db = db;
		this.cutoff = cutoff;
		this.yearService = yearService;
	}

	public List<GuesserPointsSeason> getGraph() {
		if (cutoff.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		return db.getGraph(year);
	}

	public List<RankedGuesser> getleaderboard() {
		if (cutoff.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		return db.getLeaderboard(year);
	}
}
