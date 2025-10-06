package no.vebb.f1.cutoff;

import jakarta.persistence.*;
import no.vebb.f1.race.RaceEntity;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.year.Year;

import java.time.Instant;

@Entity
@Table(name = "race_cutoffs")
public class RaceCutoffEntity {
    @EmbeddedId
    private RaceId raceId;

    @Column(name = "cutoff", nullable = false)
    private Instant cutoff;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", referencedColumnName = "race_id")
    private RaceEntity race;

    protected RaceCutoffEntity() {}

    public RaceCutoffEntity(RaceId raceId, Instant cutoff) {
        this.raceId = raceId;
        this.cutoff = cutoff;
    }

    public RaceId raceId() {
        return raceId;
    }

    public Instant cutoff() {
        return cutoff;
    }

    public String raceName() {
        return race.name();
    }

    public RacePosition position() {
        return race.position();
    }

    public Year year() {
        return race.year();
    }
}
