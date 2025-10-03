package no.vebb.f1.results.driverStandings;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

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
