package no.vebb.f1.race;

import jakarta.persistence.*;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "race_order")
public class RaceOrderEntity {
    @EmbeddedId
    private RaceId raceId;

    @Embedded
    private Year year;

    @Embedded
    private RacePosition position;

    @OneToOne
    @JoinColumn(name = "race_id")
    private RaceEntity race;

    protected RaceOrderEntity() {}

    public RaceOrderEntity(RaceId raceId, Year year, RacePosition position) {
        this.raceId = raceId;
        this.year = year;
        this.position = position;
    }

    public RaceId raceId() {
        return raceId;
    }

    public Year year() {
        return year;
    }

    public RacePosition position() {
        return position;
    }

    public String name() {
        return race.name();
    }
}
