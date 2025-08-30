package no.vebb.f1.cutoff;

import jakarta.persistence.*;
import no.vebb.f1.race.RaceEntity;
import no.vebb.f1.race.RaceOrderEntity;

import java.time.Instant;

@Entity
@Table(name = "race_cutoffs")
public class RaceCutoffEntity {
    @Id
    @Column(name = "race_id")
    private int raceId;

    @Column(name = "cutoff", nullable = false)
    private Instant cutoff;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", referencedColumnName = "race_id")
    private RaceEntity race;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", referencedColumnName = "race_id")
    private RaceOrderEntity raceOrder;

    protected RaceCutoffEntity() {}

    public RaceCutoffEntity(int raceId, Instant cutoff) {
        this.raceId = raceId;
        this.cutoff = cutoff;
    }

    public int raceId() {
        return raceId;
    }

    public Instant cutoff() {
        return cutoff;
    }

    public String raceName() {
        return race.name();
    }

    public int position() {
        return raceOrder.position();
    }

    public int year() {
        return raceOrder.year();
    }
}
