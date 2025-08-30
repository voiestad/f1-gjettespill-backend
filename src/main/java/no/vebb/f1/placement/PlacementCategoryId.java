package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlacementCategoryId {
    @Column(name = "race_id", nullable = false)
    private int raceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    protected PlacementCategoryId() {}

    public PlacementCategoryId(int raceId, UUID userId, String categoryName) {
        this.raceId = raceId;
        this.userId = userId;
        this.categoryName = categoryName;
    }

    public int raceId() {
        return raceId;
    }

    public UUID userId() {
        return userId;
    }

    public String categoryName() {
        return categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlacementCategoryId that)) return false;
        return raceId == that.raceId && Objects.equals(userId, that.userId) && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, userId, categoryName);
    }
}
