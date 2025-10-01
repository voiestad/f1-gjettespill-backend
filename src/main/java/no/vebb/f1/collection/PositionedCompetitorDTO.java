package no.vebb.f1.collection;

import no.vebb.f1.competitors.domain.CompetitorName;
import no.vebb.f1.competitors.domain.ConstructorName;
import no.vebb.f1.competitors.domain.DriverName;
import no.vebb.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.driverStandings.DriverStandingsEntity;
import no.vebb.f1.results.raceResult.RaceResultEntity;
import no.vebb.f1.results.startingGrid.StartingGridEntity;

public record PositionedCompetitorDTO<T extends CompetitorName>(String position, T name, CompetitorPoints points) {
    public static PositionedCompetitorDTO<DriverName> fromStartingGrid(StartingGridEntity startingGridEntity) {
        return new PositionedCompetitorDTO<>(String.valueOf(startingGridEntity.position()), startingGridEntity.driver().driverName(), null);
    }

    public static PositionedCompetitorDTO<DriverName> fromRaceResult(RaceResultEntity raceResultEntity) {
        return new PositionedCompetitorDTO<>(String.valueOf(raceResultEntity.position()), raceResultEntity.driver().driverName(), raceResultEntity.points());
    }

    public static PositionedCompetitorDTO<DriverName> fromDriverStandings(DriverStandingsEntity driverStandingsEntity) {
        return new PositionedCompetitorDTO<>(String.valueOf(driverStandingsEntity.position()), driverStandingsEntity.driver().driverName(), driverStandingsEntity.points());
    }

    public static PositionedCompetitorDTO<ConstructorName> fromConstructorStandings(ConstructorStandingsEntity constructorStandingsEntity) {
        return new PositionedCompetitorDTO<>(String.valueOf(constructorStandingsEntity.position()), constructorStandingsEntity.constructor().constructorName(), constructorStandingsEntity.points());
    }

}
