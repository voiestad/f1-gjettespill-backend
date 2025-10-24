package no.voiestad.f1.results.startingGrid;

import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.race.RaceId;

@Embeddable
public class StartingGridId {
    @Embedded
    private RaceId raceId;
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;

    protected StartingGridId() {}

    public StartingGridId(RaceId raceId, DriverEntity driver) {
        this.raceId = raceId;
        this.driver = driver;
    }

    public RaceId raceId() {
        return raceId;
    }

    public DriverEntity driver() {
        return driver;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StartingGridId that)) return false;
        return Objects.equals(raceId, that.raceId) && Objects.equals(driver, that.driver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, driver);
    }
}
