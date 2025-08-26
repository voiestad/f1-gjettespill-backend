package no.vebb.f1.results;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "driver_standings")
public class DriverStandings {
    @EmbeddedId
    private DriverStandingsId id;
    @Column(name = "position", nullable = false)
    private int position;
    @Column(name = "points", nullable = false)
    private int points;

    protected DriverStandings() {
    }

    public DriverStandings(int raceId, String driverName, int position, int points) {
        this.id = new DriverStandingsId(raceId, driverName);
        this.position = position;
        this.points = points;
    }

    public int raceId() {
        return id.raceId();
    }

    public String driverName() {
        return id.driverName();
    }

    public int position() {
        return position;
    }

    public int points() {
        return points;
    }

}
