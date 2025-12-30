package no.voiestad.f1.placement.leaguePlacementCategory;

import jakarta.persistence.*;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.race.RaceId;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LeaguePlacementCategoryId {
    @Embedded
    private RaceId raceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category categoryName;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    protected LeaguePlacementCategoryId() {}

    public LeaguePlacementCategoryId(RaceId raceId, UUID userId, Category categoryName, UUID leagueId) {
        this.raceId = raceId;
        this.userId = userId;
        this.categoryName = categoryName;
        this.leagueId = leagueId;
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

    public UUID leagueId(){
        return leagueId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeaguePlacementCategoryId that)) return false;
        return Objects.equals(raceId, that.raceId) && Objects.equals(userId, that.userId) && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, userId, categoryName);
    }
}
