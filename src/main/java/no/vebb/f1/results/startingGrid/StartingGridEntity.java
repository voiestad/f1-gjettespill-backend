package no.vebb.f1.results.startingGrid;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.domain.CompetitorPosition;

@Entity
@Table(name = "starting_grids")
public class StartingGridEntity {
    @EmbeddedId
    private StartingGridId id;

    @Embedded
    private CompetitorPosition position;

    protected StartingGridEntity() {
    }

    public StartingGridEntity(RaceId raceId, Driver driverName, CompetitorPosition position) {
        this.id = new StartingGridId(raceId, driverName);
        this.position = position;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public Driver driverName() {
        return id.driverName();
    }

    public CompetitorPosition position() {
        return position;
    }
}
