package no.vebb.f1.race;

import jakarta.persistence.*;
import no.vebb.f1.util.domainPrimitive.RaceId;

@Entity
@Table(name = "races")
public class RaceEntity {
    @EmbeddedId
    private RaceId raceId;

    @Column(name = "race_name", nullable = false)
    private String raceName;

    protected RaceEntity() {}

    public RaceEntity(int raceId, String raceName) {
        this.raceId = new RaceId(raceId);
        this.raceName = raceName;
    }

    public RaceId raceId() {
        return raceId;
    }

    public String name() {
        return raceName;
    }
}
