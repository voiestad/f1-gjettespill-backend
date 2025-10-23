package no.voiestad.f1.results.driverStandings;

import java.util.Objects;

import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.race.RaceId;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class DriverStandingsId {
    @Embedded
    private RaceId raceId;
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;

    protected DriverStandingsId() {
    }

    public DriverStandingsId(RaceId raceId, DriverEntity driver) {
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
        if (!(o instanceof DriverStandingsId that)) return false;
        return Objects.equals(raceId, that.raceId) && Objects.equals(driver, that.driver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, driver);
    }
}
