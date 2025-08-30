package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlacementYearId {
    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected PlacementYearId() {}

    public PlacementYearId(int year, UUID userId) {
        this.year = year;
        this.userId = userId;
    }

    public int year() {
        return year;
    }

    public UUID userId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlacementYearId that)) return false;
        return year == that.year && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, userId);
    }
}
