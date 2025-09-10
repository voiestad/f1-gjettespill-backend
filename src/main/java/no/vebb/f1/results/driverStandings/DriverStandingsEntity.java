package no.vebb.f1.results.driverStandings;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;

@Entity
@Table(name = "driver_standings")
public class DriverStandingsEntity {
    @EmbeddedId
    private DriverStandingsId id;
    @Column(name = "position", nullable = false)
    private int position;
    @Column(name = "points", nullable = false)
    private int points;

    protected DriverStandingsEntity() {
    }

    public DriverStandingsEntity(RaceId raceId, Driver driverName, int position, int points) {
        this.id = new DriverStandingsId(raceId, driverName);
        this.position = position;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public Driver driverName() {
        return id.driverName();
    }

    public int position() {
        return position;
    }

    public int points() {
        return points;
    }

}
