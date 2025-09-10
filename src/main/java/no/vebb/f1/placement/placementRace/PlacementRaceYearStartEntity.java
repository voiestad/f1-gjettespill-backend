package no.vebb.f1.placement.placementRace;

import jakarta.persistence.*;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.placement.domain.UserPosition;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "placements_race_year_start")
public class PlacementRaceYearStartEntity implements PlacementRace {
    @EmbeddedId
    private PlacementRaceYearStartId id;

    @Embedded
    private UserPosition placement;

    @Embedded
    private UserPoints points;

    protected PlacementRaceYearStartEntity() {}

    public PlacementRaceYearStartEntity(Year year, UUID userId, UserPosition placement, UserPoints points) {
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
    public UserPosition placement() {
        return placement;
    }

    @Override
    public UserPoints points() {
        return points;
    }
}
