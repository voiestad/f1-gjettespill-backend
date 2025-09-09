package no.vebb.f1.mail;

import jakarta.persistence.*;
import no.vebb.f1.race.RaceId;

import java.util.UUID;

@Entity
@Table(name = "notified")
public class NotifiedEntity {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Embedded
    private RaceId raceId;

    public NotifiedEntity() {}

    public NotifiedEntity(UUID userId, RaceId raceId) {
        this.userId = userId;
        this.raceId = raceId;
    }

    public UUID userId() {
        return userId;
    }

    public RaceId raceId() {
        return raceId;
    }
}
