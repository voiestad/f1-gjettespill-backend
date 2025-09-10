package no.vebb.f1.placement;

import jakarta.persistence.*;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.year.Year;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlacementCategoryYearStartId {
    @Embedded
    private Year year;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category categoryName;

    protected PlacementCategoryYearStartId() {}

    public PlacementCategoryYearStartId(Year year, UUID userId, Category categoryName) {
        this.year = year;
        this.userId = userId;
        this.categoryName = categoryName;
    }

    public Year year() {
        return year;
    }

    public UUID userId() {
        return userId;
    }

    public Category categoryName() {
        return categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlacementCategoryYearStartId that)) return false;
        return year == that.year && Objects.equals(userId, that.userId) && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, userId, categoryName);
    }
}
