package no.vebb.f1.mail;

import jakarta.persistence.*;
import no.vebb.f1.util.domainPrimitive.RaceId;

import java.util.UUID;

@Entity
@Table(name = "notified")
public class Notified {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "race_id", nullable = false)
    private int raceId;

    public Notified() {}

    public Notified(UUID userId, RaceId raceId) {
        this.userId = userId;
        this.raceId = raceId.value;
    }

    public UUID userId() {
        return userId;
    }

    public RaceId raceId() {
        return new RaceId(raceId);
    }
}
