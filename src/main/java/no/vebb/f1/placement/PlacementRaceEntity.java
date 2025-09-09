package no.vebb.f1.placement;

import jakarta.persistence.*;
import no.vebb.f1.race.RaceId;

import java.util.UUID;

@Entity
@Table(name = "placements_race")
public class PlacementRaceEntity implements PlacementRace {
    @EmbeddedId
    private PlacementRaceId id;

    @Column(name = "placement", nullable = false)
    private int placement;

    @Column(name = "points", nullable = false)
    private int points;

    protected PlacementRaceEntity() {}

    public PlacementRaceEntity(RaceId raceId, UUID userId, int placement, int points) {
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
    public int placement() {
        return placement;
    }

    @Override
    public int points() {
        return points;
    }
}
