package no.vebb.f1.collection;

import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.domain.Competitor;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.driverStandings.DriverStandingsEntity;
import no.vebb.f1.results.raceResult.RaceResultEntity;
import no.vebb.f1.results.startingGrid.StartingGridEntity;

public record PositionedCompetitor<T extends Competitor>(String position, T name, CompetitorPoints points) {
    public static PositionedCompetitor<DriverEntity> fromStartingGrid(StartingGridEntity startingGridEntity) {
        return new PositionedCompetitor<>(String.valueOf(startingGridEntity.position()), startingGridEntity.driver(), null);
    }

    public static PositionedCompetitor<DriverEntity> fromRaceResult(RaceResultEntity raceResultEntity) {
        return new PositionedCompetitor<>(String.valueOf(raceResultEntity.position()), raceResultEntity.driver(), raceResultEntity.points());
    }

    public static PositionedCompetitor<DriverEntity> fromDriverStandings(DriverStandingsEntity driverStandingsEntity) {
        return new PositionedCompetitor<>(String.valueOf(driverStandingsEntity.position()), driverStandingsEntity.driver(), driverStandingsEntity.points());
    }

    public static PositionedCompetitor<ConstructorEntity> fromConstructorStandings(ConstructorStandingsEntity constructorStandingsEntity) {
        return new PositionedCompetitor<>(String.valueOf(constructorStandingsEntity.position()), constructorStandingsEntity.constructor(), constructorStandingsEntity.points());
    }

}
