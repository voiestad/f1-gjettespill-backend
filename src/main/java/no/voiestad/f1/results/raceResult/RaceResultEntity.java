package no.voiestad.f1.results.raceResult;

import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.results.domain.CompetitorPosition;

import jakarta.persistence.*;

@Entity
@Table(name = "race_results")
public class RaceResultEntity {
    @EmbeddedId
    private RaceResultId id;
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;

    protected RaceResultEntity() {}

    public RaceResultEntity(RaceId raceId, CompetitorPosition position, DriverEntity driver) {
        this.id = new RaceResultId(raceId, position);
        this.driver = driver;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public CompetitorPosition position() {
        return id.position();
    }

    public DriverEntity driver() {
        return driver;
    }
}
