package no.vebb.f1.placement.placementRace;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.year.Year;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlacementRaceYearStartId {
    @Embedded
    private Year year;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected PlacementRaceYearStartId() {}

    public PlacementRaceYearStartId(Year year, UUID userId) {
        this.year = year;
        this.userId = userId;
    }

    public Year year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlacementRaceYearStartId that)) return false;
        return Objects.equals(year, that.year) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, userId);
    }

    public UUID userId() {
        return userId;
    }
}
