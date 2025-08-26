package no.vebb.f1.util.collection;

import no.vebb.f1.results.ConstructorStandings;
import no.vebb.f1.results.DriverStandings;
import no.vebb.f1.results.RaceResult;
import no.vebb.f1.results.StartingGrid;

public record PositionedCompetitor(String position, String name, int points) {
    public static PositionedCompetitor fromStartingGrid(StartingGrid startingGrid) {
        return new PositionedCompetitor(String.valueOf(startingGrid.position()), startingGrid.driverName(), 0);
    }

    public static PositionedCompetitor fromRaceResult(RaceResult raceResult) {
        return new PositionedCompetitor(String.valueOf(raceResult.position()), raceResult.driverName(), raceResult.points());
    }

    public static PositionedCompetitor fromDriverStandings(DriverStandings driverStandings) {
        return new PositionedCompetitor(String.valueOf(driverStandings.position()), driverStandings.driverName(), driverStandings.points());
    }

    public static PositionedCompetitor fromConstructorStandings(ConstructorStandings constructorStandings) {
        return new PositionedCompetitor(String.valueOf(constructorStandings.position()), constructorStandings.constructorName(), constructorStandings.points());
    }

}
