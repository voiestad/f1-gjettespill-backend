package no.vebb.f1.results.startingGrid;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;

@Entity
@Table(name = "starting_grids")
public class StartingGridEntity {
    @EmbeddedId
    private StartingGridId id;

    @Column(name = "position", nullable = false)
    private int position;

    protected StartingGridEntity() {
    }

    public StartingGridEntity(RaceId raceId, Driver driverName, int position) {
        this.id = new StartingGridId(raceId, driverName);
        this.position = position;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public Driver driverName() {
        return id.driverName();
    }

    public int position() {
        return position;
    }
}
