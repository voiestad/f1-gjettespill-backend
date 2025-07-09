package no.vebb.f1.util;

import java.util.List;

import no.vebb.f1.database.Database;
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

	public RaceStats(RaceId raceId, Year year, Database db) {
		this.startingGrid = db.getStartingGrid(raceId);
		this.raceResult = db.getRaceResult(raceId);
		this.driverStandings = db.getDriverStandings(raceId);
		this.constructorStandings = db.getConstructorStandings(raceId);
		this.flags = db.getRegisteredFlags(raceId);
		int position = db.getPositionOfRace(raceId);
		String raceName = db.getRaceName(raceId);
		this.name = String.format("%d. %s %d", position, raceName, year.value);
	}
}
