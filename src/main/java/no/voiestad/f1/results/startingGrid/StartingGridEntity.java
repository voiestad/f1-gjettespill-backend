package no.voiestad.f1.results.startingGrid;

import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.results.domain.CompetitorPosition;

import jakarta.persistence.*;

@Entity
@Table(name = "starting_grids")
public class StartingGridEntity {
    @EmbeddedId
    private StartingGridId id;

    @Embedded
    private CompetitorPosition position;

    protected StartingGridEntity() {
    }

    public StartingGridEntity(RaceId raceId, DriverEntity driver, CompetitorPosition position) {
        this.id = new StartingGridId(raceId, driver);
        this.position = position;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public CompetitorPosition position() {
        return position;
    }

    public DriverEntity driver() {
        return id.driver();
    }
}
