package no.vebb.f1.results.driverStandings;

import jakarta.persistence.*;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.domain.CompetitorPosition;

@Entity
@Table(name = "driver_standings")
public class DriverStandingsEntity {
    @EmbeddedId
    private DriverStandingsId id;
    @Embedded
    private CompetitorPosition position;
    @Embedded
    private CompetitorPoints points;

    protected DriverStandingsEntity() {
    }

    public DriverStandingsEntity(RaceId raceId, DriverEntity driver, CompetitorPosition position, CompetitorPoints points) {
        this.id = new DriverStandingsId(raceId, driver);
        this.position = position;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public DriverEntity driver() {
        return id.driver();
    }

    public CompetitorPosition position() {
        return position;
    }

    public CompetitorPoints points() {
        return points;
    }

}
