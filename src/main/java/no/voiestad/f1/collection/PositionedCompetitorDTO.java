package no.voiestad.f1.collection;

import no.voiestad.f1.competitors.domain.CompetitorId;
import no.voiestad.f1.competitors.domain.CompetitorName;
import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.voiestad.f1.results.domain.CompetitorPoints;
import no.voiestad.f1.results.driverStandings.DriverStandingsEntity;
import no.voiestad.f1.results.raceResult.RaceResultEntity;
import no.voiestad.f1.results.startingGrid.StartingGridEntity;

public record PositionedCompetitorDTO<T extends CompetitorName>(
        String position, T name, CompetitorPoints points, CompetitorId id) {

    public static PositionedCompetitorDTO<DriverName> fromStartingGrid(StartingGridEntity entity) {
        return new PositionedCompetitorDTO<>(String.valueOf(entity.position()), entity.driver().driverName(),
                null, entity.driver().driverId());
    }

    public static PositionedCompetitorDTO<DriverName> fromRaceResult(RaceResultEntity entity) {
        return new PositionedCompetitorDTO<>(String.valueOf(entity.position()), entity.driver().driverName(),
                entity.points(), entity.driver().driverId());
    }

    public static PositionedCompetitorDTO<DriverName> fromDriverStandings(DriverStandingsEntity entity) {
        return new PositionedCompetitorDTO<>(String.valueOf(entity.position()), entity.driver().driverName(),
                entity.points(), entity.driver().driverId());
    }

    public static PositionedCompetitorDTO<ConstructorName> fromConstructorStandings(ConstructorStandingsEntity entity) {
        return new PositionedCompetitorDTO<>(String.valueOf(entity.position()), entity.constructor().constructorName(),
                entity.points(), entity.constructor().constructorId());
    }

}
