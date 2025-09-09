package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.util.domainPrimitive.RaceId;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlacementRaceId {
    @Embedded
    private RaceId raceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected PlacementRaceId() {}

    public PlacementRaceId(RaceId raceId, UUID userId) {
        this.raceId = raceId;
        this.userId = userId;
    }

    public RaceId raceId() {
        return raceId;
    }

    public UUID userId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlacementRaceId that)) return false;
        return raceId == that.raceId && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, userId);
    }
}
