package no.voiestad.f1.collection;

import no.voiestad.f1.competitors.domain.CompetitorName;
import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.voiestad.f1.results.domain.CompetitorPoints;
import no.voiestad.f1.results.driverStandings.DriverStandingsEntity;
import no.voiestad.f1.results.raceResult.RaceResultEntity;
import no.voiestad.f1.results.startingGrid.StartingGridEntity;

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
