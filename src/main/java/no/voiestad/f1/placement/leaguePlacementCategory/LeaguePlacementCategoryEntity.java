package no.voiestad.f1.placement.leaguePlacementCategory;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.race.RaceId;

import java.util.UUID;

@Entity
@Table(name = "league_placements_category")
public class LeaguePlacementCategoryEntity {
    @EmbeddedId
    private LeaguePlacementCategoryId id;

    @Embedded
    private UserPosition placement;

    protected LeaguePlacementCategoryEntity() {}

    public LeaguePlacementCategoryEntity(RaceId raceId, UUID userId, Category categoryName, UUID leagueId, UserPosition placement) {
        this.id = new LeaguePlacementCategoryId(raceId, userId, categoryName, leagueId);
        this.placement = placement;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public UUID userId() {
        return id.userId();
    }

    public Category categoryName() {
        return id.categoryName();
    }

    public UUID leagueId() {
        return id.leagueId();
    }

    public UserPosition placement() {
        return placement;
    }
}
