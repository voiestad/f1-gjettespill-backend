package no.vebb.f1.stats;

import jakarta.persistence.*;
import no.vebb.f1.race.RaceId;

@Entity
@Table(name = "flag_stats")
public class FlagStatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "flag_id")
    private int flagId;

    @Column(name = "flag_name")
    private String flagName;

    @Embedded
    private RaceId raceId;

    @Column(name = "round")
    private int round;

    @Column(name = "session_type")
    private String sessionType;

    protected FlagStatEntity() {}

    public FlagStatEntity(String flagName, RaceId raceId, int round, String sessionType) {
        this.flagName = flagName;
        this.raceId = raceId;
        this.round = round;
        this.sessionType = sessionType;
    }

    public int flagId() {
        return flagId;
    }

    public String flagName() {
        return flagName;
    }

    public RaceId raceId() {
        return raceId;
    }

    public int round() {
        return round;
    }

    public String sessionType() {
        return sessionType;
    }
}
