package no.voiestad.f1.placement.placementCategory;

import java.util.UUID;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.race.RaceId;

import jakarta.persistence.*;

@Entity
@Table(name = "placements_category")
public class PlacementCategoryEntity implements PlacementCategory {
    @EmbeddedId
    private PlacementCategoryId id;

    @Embedded
    private UserPosition placement;

    @Embedded
    private UserPoints points;

    protected PlacementCategoryEntity() {}

    public PlacementCategoryEntity(RaceId raceId, UUID userId, Category categoryName, UserPosition placement, UserPoints points) {
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
    public UserPosition placement() {
        return placement;
    }

    @Override
    public UserPoints points() {
        return points;
    }
}
