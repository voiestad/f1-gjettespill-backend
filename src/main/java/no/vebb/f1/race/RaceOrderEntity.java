package no.vebb.f1.race;

import jakarta.persistence.*;
import no.vebb.f1.util.domainPrimitive.Year;

@Entity
@Table(name = "race_order")
public class RaceOrderEntity {
    @Id
    @Column(name = "race_id")
    private int raceId;

    @Embedded
    private Year year;

    @Column(name = "position", nullable = false)
    private int position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", referencedColumnName = "race_id")
    private RaceEntity race;

    protected RaceOrderEntity() {}

    public RaceOrderEntity(int raceId, Year year, int position) {
        this.raceId = raceId;
        this.year = year;
        this.position = position;
    }

    public int raceId() {
        return raceId;
    }

    public Year year() {
        return year;
    }

    public int position() {
        return position;
    }

    public String name() {
        return race.name();
    }
}
