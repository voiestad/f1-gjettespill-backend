package no.vebb.f1.placement.placementRace;

import jakarta.persistence.*;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.placement.domain.UserPosition;
import no.vebb.f1.race.RaceId;

import java.util.UUID;

@Entity
@Table(name = "placements_race")
public class PlacementRaceEntity implements PlacementRace {
    @EmbeddedId
    private PlacementRaceId id;

    @Embedded
    private UserPosition placement;

    @Embedded
    private UserPoints points;

    protected PlacementRaceEntity() {}

    public PlacementRaceEntity(RaceId raceId, UUID userId, UserPosition placement, UserPoints points) {
        this.id = new PlacementRaceId(raceId, userId);
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
    public UserPosition placement() {
        return placement;
    }

    @Override
    public UserPoints points() {
        return points;
    }
}
