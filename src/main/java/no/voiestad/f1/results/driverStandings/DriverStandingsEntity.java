package no.voiestad.f1.results.driverStandings;

import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.results.domain.CompetitorPosition;

import jakarta.persistence.*;

@Entity
@Table(name = "driver_standings")
public class DriverStandingsEntity {
    @EmbeddedId
    private DriverStandingsId id;
    @Embedded
    private CompetitorPosition position;

    protected DriverStandingsEntity() {
    }

    public DriverStandingsEntity(RaceId raceId, DriverEntity driver, CompetitorPosition position) {
        this.id = new DriverStandingsId(raceId, driver);
        this.position = position;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public DriverEntity driver() {
        return id.driver();
    }

    public CompetitorPosition position() {
        return position;
    }

}
