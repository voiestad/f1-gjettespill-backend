package no.vebb.f1.util.collection;

import no.vebb.f1.results.ConstructorStandingsEntity;
import no.vebb.f1.results.DriverStandingsEntity;
import no.vebb.f1.results.RaceResultEntity;
import no.vebb.f1.results.StartingGridEntity;

public record PositionedCompetitor(String position, String name, int points) {
    public static PositionedCompetitor fromStartingGrid(StartingGridEntity startingGridEntity) {
        return new PositionedCompetitor(String.valueOf(startingGridEntity.position()), startingGridEntity.driverName(), 0);
    }

    public static PositionedCompetitor fromRaceResult(RaceResultEntity raceResultEntity) {
        return new PositionedCompetitor(String.valueOf(raceResultEntity.position()), raceResultEntity.driverName(), raceResultEntity.points());
    }

    public static PositionedCompetitor fromDriverStandings(DriverStandingsEntity driverStandingsEntity) {
        return new PositionedCompetitor(String.valueOf(driverStandingsEntity.position()), driverStandingsEntity.driverName(), driverStandingsEntity.points());
    }

    public static PositionedCompetitor fromConstructorStandings(ConstructorStandingsEntity constructorStandingsEntity) {
        return new PositionedCompetitor(String.valueOf(constructorStandingsEntity.position()), constructorStandingsEntity.constructorName(), constructorStandingsEntity.points());
    }

}
