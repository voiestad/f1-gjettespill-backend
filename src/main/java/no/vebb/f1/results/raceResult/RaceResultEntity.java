package no.vebb.f1.results.raceResult;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.domain.CompetitorPosition;

@Entity
@Table(name = "race_results")
public class RaceResultEntity {
    @EmbeddedId
    private RaceResultId id;
    @Column(name = "qualified_position", nullable = false)
    private String position;
    @Embedded
    private Driver driverName;
    @Embedded
    private CompetitorPoints points;

    protected RaceResultEntity() {}

    public RaceResultEntity(RaceId raceId, CompetitorPosition finishingPosition, String position, Driver driverName, CompetitorPoints points) {
        this.id = new RaceResultId(raceId, finishingPosition);
        this.position = position;
        this.driverName = driverName;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public CompetitorPosition finishingPosition() {
        return id.finishingPosition();
    }

    public String position() {
        return position;
    }

    public Driver driverName() {
        return driverName;
    }

    public CompetitorPoints points() {
        return points;
    }
}
