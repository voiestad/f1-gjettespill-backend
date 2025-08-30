package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlacementCategoryYearStartId {
    @Column(name = "year")
    private int year;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "category_name")
    private String categoryName;

    protected PlacementCategoryYearStartId() {}

    public PlacementCategoryYearStartId(int year, UUID userId, String categoryName) {
        this.year = year;
        this.userId = userId;
        this.categoryName = categoryName;
    }

    public int year() {
        return year;
    }

    public UUID userId() {
        return userId;
    }

    public String categoryName() {
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
