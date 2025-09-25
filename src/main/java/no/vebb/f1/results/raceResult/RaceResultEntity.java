package no.vebb.f1.results.raceResult;

import jakarta.persistence.*;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.competitors.driver.DriverId;
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
    private DriverId driverId;
    @Embedded
    private CompetitorPoints points;
    @ManyToOne
    @JoinColumn(name = "driver_id", insertable = false, updatable = false)
    private DriverEntity driver;

    protected RaceResultEntity() {}

    public RaceResultEntity(RaceId raceId, CompetitorPosition finishingPosition, String position, DriverId driverId, CompetitorPoints points) {
        this.id = new RaceResultId(raceId, finishingPosition);
        this.position = position;
        this.driverId = driverId;
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

    public DriverEntity driver() {
        return driver;
    }

    public CompetitorPoints points() {
        return points;
    }
}
