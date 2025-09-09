package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.UUID;

@Entity
@Table(name = "placements_category_year_start")
public class PlacementCategoryYearStartEntity implements PlacementCategory {
    @EmbeddedId
    private PlacementCategoryYearStartId id;

    @Column(name = "placement", nullable = false)
    private int placement;

    @Column(name = "points", nullable = false)
    private int points;

    protected PlacementCategoryYearStartEntity() {}

    public PlacementCategoryYearStartEntity(Year year, UUID userId, String categoryName, int placement, int points) {
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
    public String categoryName() {
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
