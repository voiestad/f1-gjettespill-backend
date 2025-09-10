package no.vebb.f1.placement.placementCategory;

import jakarta.persistence.*;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.race.RaceId;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlacementCategoryId {
    @Embedded
    private RaceId raceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category categoryName;

    protected PlacementCategoryId() {}

    public PlacementCategoryId(RaceId raceId, UUID userId, Category categoryName) {
        this.raceId = raceId;
        this.userId = userId;
        this.categoryName = categoryName;
    }

    public RaceId raceId() {
        return raceId;
    }

    public UUID userId() {
        return userId;
    }

    public Category categoryName() {
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
