package no.vebb.f1.collection;

import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.driverStandings.DriverStandingsEntity;
import no.vebb.f1.results.raceResult.RaceResultEntity;

public record PositionedCompetitor<T>(String position, T entity, CompetitorPoints points) {
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
