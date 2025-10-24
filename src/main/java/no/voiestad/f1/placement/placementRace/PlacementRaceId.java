package no.voiestad.f1.placement.placementRace;

import java.util.Objects;
import java.util.UUID;

import no.voiestad.f1.race.RaceId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

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
        return Objects.equals(raceId, that.raceId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, userId);
    }
}
