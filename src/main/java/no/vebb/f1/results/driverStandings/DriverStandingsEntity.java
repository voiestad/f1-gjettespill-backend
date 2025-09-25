package no.vebb.f1.results.driverStandings;

import jakarta.persistence.*;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.competitors.driver.DriverId;
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
    @ManyToOne
    @JoinColumn(name = "driver_id", insertable = false, updatable = false)
    private DriverEntity driver;

    protected DriverStandingsEntity() {
    }

    public DriverStandingsEntity(RaceId raceId, DriverId driverId, CompetitorPosition position, CompetitorPoints points) {
        this.id = new DriverStandingsId(raceId, driverId);
        this.position = position;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public DriverEntity driver() {
        return driver;
    }

    public CompetitorPosition position() {
        return position;
    }

    public CompetitorPoints points() {
        return points;
    }

}
