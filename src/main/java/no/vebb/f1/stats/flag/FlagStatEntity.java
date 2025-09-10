package no.vebb.f1.stats.flag;

import jakarta.persistence.*;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.stats.domain.SessionType;

@Entity
@Table(name = "flag_stats")
public class FlagStatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "flag_id")
    private int flagId;

    @Column(name = "flag_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Flag flagName;

    @Embedded
    private RaceId raceId;

    @Column(name = "round", nullable = false)
    private int round;

    @Column(name = "session_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    protected FlagStatEntity() {}

    public FlagStatEntity(Flag flagName, RaceId raceId, int round, SessionType sessionType) {
        this.flagName = flagName;
        this.raceId = raceId;
        this.round = round;
        this.sessionType = sessionType;
    }

    public int flagId() {
        return flagId;
    }

    public Flag flagName() {
        return flagName;
    }

    public RaceId raceId() {
        return raceId;
    }

    public int round() {
        return round;
    }

    public SessionType sessionType() {
        return sessionType;
    }
}
