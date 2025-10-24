package no.voiestad.f1.placement.placementCategory;

import java.util.UUID;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

@Entity
@Table(name = "placements_category_year_start")
public class PlacementCategoryYearStartEntity implements PlacementCategory {
    @EmbeddedId
    private PlacementCategoryYearStartId id;

    @Embedded
    private UserPosition placement;

    @Embedded
    private UserPoints points;

    protected PlacementCategoryYearStartEntity() {}

    public PlacementCategoryYearStartEntity(Year year, UUID userId, Category categoryName, UserPosition placement, UserPoints points) {
        this.id = new PlacementCategoryYearStartId(year, userId, categoryName);
        this.placement = placement;
        this.points = points;
    }

    public Year year() {
        return id.year();
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
