package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.guessing.Category;
import no.vebb.f1.race.RaceId;

import java.util.UUID;

@Entity
@Table(name = "placements_category")
public class PlacementCategoryEntity implements PlacementCategory {
    @EmbeddedId
    private PlacementCategoryId id;

    @Column(name = "placement", nullable = false)
    private int placement;

    @Column(name = "points", nullable = false)
    private int points;

    protected PlacementCategoryEntity() {}

    public PlacementCategoryEntity(RaceId raceId, UUID userId, Category categoryName, int placement, int points) {
        this.id = new PlacementCategoryId(raceId, userId, categoryName);
        this.placement = placement;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public UUID userId() {
        return id.userId();
    }

    @Override
    public Category categoryName() {
        return id.categoryName();
    }

    @Override
    public int placement() {
        return placement;
    }

    @Override
    public int points() {
        return points;
    }
}
