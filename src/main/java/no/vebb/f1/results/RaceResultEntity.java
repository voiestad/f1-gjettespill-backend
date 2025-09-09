package no.vebb.f1.results;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;

@Entity
@Table(name = "race_results")
public class RaceResultEntity {
    @EmbeddedId
    private RaceResultId id;
    @Column(name = "position", nullable = false)
    private String position;
    @Embedded
    private Driver driverName;
    @Column(name = "points", nullable = false)
    private int points;

    protected RaceResultEntity() {}

    public RaceResultEntity(RaceId raceId, int finishingPosition, String position, Driver driverName, int points) {
        this.id = new RaceResultId(raceId, finishingPosition);
        this.position = position;
        this.driverName = driverName;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public int finishingPosition() {
        return id.finishingPosition();
    }

    public String position() {
        return position;
    }

    public Driver driverName() {
        return driverName;
    }

    public int points() {
        return points;
    }
}
