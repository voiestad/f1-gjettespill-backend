package no.vebb.f1.graph;

import java.util.List;

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

	public Graph(Database db, Cutoff cutoff) {
		this.db = db;
		this.cutoff = cutoff;
	}

	public List<GuesserPointsSeason> getGraph() {
		if (cutoff.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		return db.getGraph(year);
	}

	public List<RankedGuesser> getleaderboard() {
		if (cutoff.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), db);
		return db.getLeaderboard(year);
	}
}
