package no.vebb.f1.race;

import jakarta.persistence.*;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "races")
public class RaceEntity {
    @EmbeddedId
    private RaceId raceId;

    @Column(name = "race_name", nullable = false)
    private String raceName;

    @Embedded
    private Year year;

    @Embedded
    private RacePosition position;

    protected RaceEntity() {}

    public RaceEntity(RaceId raceId, String raceName, Year year, RacePosition position) {
        this.raceId = raceId;
        this.raceName = raceName;
        this.year = year;
        this.position = position;
    }

    public RaceId raceId() {
        return raceId;
    }

    public String name() {
        return raceName;
    }

    public Year year() {
        return year;
    }

    public RacePosition position() {
        return position;
    }

    public RaceEntity withPosition(RacePosition newPosition) {
        return new RaceEntity(raceId, raceName, year, newPosition);
    }
}
