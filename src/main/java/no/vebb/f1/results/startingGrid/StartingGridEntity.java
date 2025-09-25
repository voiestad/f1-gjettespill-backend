package no.vebb.f1.results.startingGrid;

import jakarta.persistence.*;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.competitors.driver.DriverId;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.domain.CompetitorPosition;

@Entity
@Table(name = "starting_grids")
public class StartingGridEntity {
    @EmbeddedId
    private StartingGridId id;

    @Embedded
    private CompetitorPosition position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", insertable = false, updatable = false)
    private DriverEntity driver;

    protected StartingGridEntity() {
    }

    public StartingGridEntity(RaceId raceId, DriverId driverId, CompetitorPosition position) {
        this.id = new StartingGridId(raceId, driverId);
        this.position = position;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public CompetitorPosition position() {
        return position;
    }

    public DriverEntity driver() {
        return driver;
    }
}
