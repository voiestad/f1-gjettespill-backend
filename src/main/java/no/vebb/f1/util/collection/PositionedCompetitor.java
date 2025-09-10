package no.vebb.f1.util.collection;

import no.vebb.f1.competitors.domain.Competitor;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.driverStandings.DriverStandingsEntity;
import no.vebb.f1.results.raceResult.RaceResultEntity;
import no.vebb.f1.results.startingGrid.StartingGridEntity;

public record PositionedCompetitor<T extends Competitor>(String position, T name, CompetitorPoints points) {
    public static PositionedCompetitor<Driver> fromStartingGrid(StartingGridEntity startingGridEntity) {
        return new PositionedCompetitor<>(String.valueOf(startingGridEntity.position()), startingGridEntity.driverName(), new CompetitorPoints());
    }

    public static PositionedCompetitor<Driver> fromRaceResult(RaceResultEntity raceResultEntity) {
        return new PositionedCompetitor<>(String.valueOf(raceResultEntity.position()), raceResultEntity.driverName(), raceResultEntity.points());
    }

    public static PositionedCompetitor<Driver> fromDriverStandings(DriverStandingsEntity driverStandingsEntity) {
        return new PositionedCompetitor<>(String.valueOf(driverStandingsEntity.position()), driverStandingsEntity.driverName(), driverStandingsEntity.points());
    }

    public static PositionedCompetitor<Constructor> fromConstructorStandings(ConstructorStandingsEntity constructorStandingsEntity) {
        return new PositionedCompetitor<>(String.valueOf(constructorStandingsEntity.position()), constructorStandingsEntity.constructorName(), constructorStandingsEntity.points());
    }

}
