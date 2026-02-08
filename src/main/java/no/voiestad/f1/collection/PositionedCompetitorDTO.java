package no.voiestad.f1.collection;

import no.voiestad.f1.competitors.domain.CompetitorId;
import no.voiestad.f1.competitors.domain.CompetitorName;
import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.results.constructorStandings.ConstructorStandingsEntity;
import no.voiestad.f1.results.domain.CompetitorPosition;
import no.voiestad.f1.results.driverStandings.DriverStandingsEntity;
import no.voiestad.f1.results.raceResult.RaceResultEntity;
import no.voiestad.f1.results.startingGrid.StartingGridEntity;

public record PositionedCompetitorDTO<T extends CompetitorName>(CompetitorPosition position, T name, CompetitorId id) {

    public static PositionedCompetitorDTO<DriverName> fromStartingGrid(StartingGridEntity entity) {
        return new PositionedCompetitorDTO<>(entity.position(), entity.driver().driverName(), entity.driver().driverId());
    }

    public static PositionedCompetitorDTO<DriverName> fromRaceResult(RaceResultEntity entity) {
        return new PositionedCompetitorDTO<>(entity.position(), entity.driver().driverName(), entity.driver().driverId());
    }

    public static PositionedCompetitorDTO<DriverName> fromDriverStandings(DriverStandingsEntity entity) {
        return new PositionedCompetitorDTO<>(entity.position(), entity.driver().driverName(), entity.driver().driverId());
    }

    public static PositionedCompetitorDTO<ConstructorName> fromConstructorStandings(ConstructorStandingsEntity entity) {
        return new PositionedCompetitorDTO<>(entity.position(), entity.constructor().constructorName(), entity.constructor().constructorId());
    }

}
