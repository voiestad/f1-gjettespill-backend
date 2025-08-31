package no.vebb.f1.util;

import java.util.List;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.stats.StatsService;
import no.vebb.f1.util.collection.PositionedCompetitor;
import no.vebb.f1.util.collection.RegisteredFlag;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;

public class RaceStats {

	public final String name;
	public final List<PositionedCompetitor> startingGrid;
	public final List<PositionedCompetitor> raceResult;
	public final List<PositionedCompetitor> driverStandings;
	public final List<PositionedCompetitor> constructorStandings;
	public final List<RegisteredFlag> flags;

	public RaceStats(RaceId raceId, Year year, ResultService resultService, RaceService raceService, StatsService statsService) {
		this.startingGrid = resultService.getStartingGrid(raceId).stream().map(PositionedCompetitor::fromStartingGrid).toList();
		this.raceResult = resultService.getRaceResult(raceId).stream().map(PositionedCompetitor::fromRaceResult).toList();
		this.driverStandings = resultService.getDriverStandings(raceId).stream().map(PositionedCompetitor::fromDriverStandings).toList();
		this.constructorStandings = resultService.getConstructorStandings(raceId).stream().map(PositionedCompetitor::fromConstructorStandings).toList();
		this.flags = statsService.getRegisteredFlags(raceId);
		int position = raceService.getPositionOfRace(raceId);
		String raceName = raceService.getRaceFromId(raceId).name();
		this.name = String.format("%d. %s %d", position, raceName, year.value);
	}
}
