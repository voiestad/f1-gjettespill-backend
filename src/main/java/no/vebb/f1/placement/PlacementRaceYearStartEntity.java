package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.UUID;

@Entity
@Table(name = "placements_race_year_start")
public class PlacementRaceYearStartEntity implements PlacementRace {
    @EmbeddedId
    private PlacementRaceYearStartId id;

    @Column(name = "placement", nullable = false)
    private int placement;

    @Column(name = "points", nullable = false)
    private int points;

    protected PlacementRaceYearStartEntity() {}

    public PlacementRaceYearStartEntity(Year year, UUID userId, int placement, int points) {
        this.id = new PlacementRaceYearStartId(year, userId);
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
    public int placement() {
        return placement;
    }

    @Override
    public int points() {
        return points;
    }
}
