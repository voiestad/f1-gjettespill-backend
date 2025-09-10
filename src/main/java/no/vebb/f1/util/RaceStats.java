package no.vebb.f1.util;

import java.util.List;

import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.race.RaceService;
import no.vebb.f1.results.ResultService;
import no.vebb.f1.stats.StatsService;
import no.vebb.f1.collection.PositionedCompetitor;
import no.vebb.f1.collection.RegisteredFlag;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

public class RaceStats {

	public final String name;
	public final List<PositionedCompetitor<Driver>> startingGrid;
	public final List<PositionedCompetitor<Driver>> raceResult;
	public final List<PositionedCompetitor<Driver>> driverStandings;
	public final List<PositionedCompetitor<Constructor>> constructorStandings;
	public final List<RegisteredFlag> flags;

	public RaceStats(RaceId raceId, Year year, ResultService resultService, RaceService raceService, StatsService statsService) {
		this.startingGrid = resultService.getStartingGrid(raceId).stream().map(PositionedCompetitor::fromStartingGrid).toList();
		this.raceResult = resultService.getRaceResult(raceId).stream().map(PositionedCompetitor::fromRaceResult).toList();
		this.driverStandings = resultService.getDriverStandings(raceId).stream().map(PositionedCompetitor::fromDriverStandings).toList();
		this.constructorStandings = resultService.getConstructorStandings(raceId).stream().map(PositionedCompetitor::fromConstructorStandings).toList();
		this.flags = statsService.getRegisteredFlags(raceId);
		RacePosition position = raceService.getPositionOfRace(raceId);
		String raceName = raceService.getRaceFromId(raceId).name();
		this.name = String.format("%s. %s %s", position, raceName, year);
	}
}
