package no.vebb.f1.graph;

import java.util.List;

import no.vebb.f1.placement.PlacementService;
import no.vebb.f1.year.YearService;
import org.springframework.stereotype.Component;

import no.vebb.f1.cutoff.CutoffService;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.collection.RankedGuesser;
import no.vebb.f1.util.domainPrimitive.Year;

@Component
public class Graph {

	private final CutoffService cutoffService;
	private final YearService yearService;
	private final PlacementService placementService;

	public Graph(CutoffService cutoffService, YearService yearService, PlacementService placementService) {
		this.cutoffService = cutoffService;
		this.yearService = yearService;
		this.placementService = placementService;
	}

	public List<GuesserPointsSeason> getGraph() {
		if (cutoffService.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		return placementService.getGraph(year);
	}

	public List<RankedGuesser> getleaderboard() {
		if (cutoffService.isAbleToGuessCurrentYear()) {
			return null;
		}
		Year year = new Year(TimeUtil.getCurrentYear(), yearService);
		return placementService.getLeaderboard(year);
	}
}
