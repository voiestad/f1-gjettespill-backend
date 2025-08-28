package no.vebb.f1.results;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "race_results")
public class RaceResultEntity {
    @EmbeddedId
    private RaceResultId id;
    @Column(name = "position", nullable = false)
    private String position;
    @Column(name = "driver_name", nullable = false)
    private String driverName;
    @Column(name = "points", nullable = false)
    private int points;

    protected RaceResultEntity() {}

    public RaceResultEntity(int raceId, int finishingPosition, String position, String driverName, int points) {
        this.id = new RaceResultId(raceId, finishingPosition);
        this.position = position;
        this.driverName = driverName;
        this.points = points;
    }

    public int raceId() {
        return id.raceId();
    }

    public int finishingPosition() {
        return id.finishingPosition();
    }

    public String position() {
        return position;
    }

    public String driverName() {
        return driverName;
    }

    public int points() {
        return points;
    }
}
