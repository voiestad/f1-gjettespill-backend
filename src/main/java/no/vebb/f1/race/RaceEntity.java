package no.vebb.f1.race;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "races")
public class RaceEntity {
    @Id
    @Column(name = "race_id")
    private int raceId;

    @Column(name = "race_name")
    private String raceName;

    protected RaceEntity() {}

    public RaceEntity(int raceId, String raceName) {
        this.raceId = raceId;
        this.raceName = raceName;
    }

    public int raceId() {
        return raceId;
    }

    public String name() {
        return raceName;
    }
}
