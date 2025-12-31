package no.voiestad.f1.placement.leaguePlacementCategory;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "league_placements_category_year_start")
public class LeaguePlacementCategoryYearStartEntity {
    @EmbeddedId
    private LeaguePlacementCategoryYearStartId id;

    @Embedded
    private UserPosition placement;

    protected LeaguePlacementCategoryYearStartEntity() {}

    public LeaguePlacementCategoryYearStartEntity(Year year, UUID userId, Category categoryName, UUID leagueId, UserPosition placement) {
        this.id = new LeaguePlacementCategoryYearStartId(year, userId, categoryName, leagueId);
        this.placement = placement;
    }

    public Year year() {
        return id.year();
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
