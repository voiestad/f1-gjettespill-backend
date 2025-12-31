package no.voiestad.f1.placement.leaguePlacementCategory;

import jakarta.persistence.*;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.year.Year;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LeaguePlacementCategoryYearStartId {
    @Embedded
    private Year year;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category categoryName;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    protected LeaguePlacementCategoryYearStartId() {}

    public LeaguePlacementCategoryYearStartId(Year year, UUID userId, Category categoryName, UUID leagueId) {
        this.year = year;
        this.userId = userId;
        this.categoryName = categoryName;
        this.leagueId = leagueId;
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

    public UUID leagueId() {
        return leagueId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LeaguePlacementCategoryYearStartId that = (LeaguePlacementCategoryYearStartId) o;
        return Objects.equals(year, that.year) && Objects.equals(userId, that.userId) && categoryName == that.categoryName && Objects.equals(leagueId, that.leagueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, userId, categoryName, leagueId);
    }
}
