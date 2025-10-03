package no.vebb.f1.results.driverStandings;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.competitors.driver.DriverId;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

@Embeddable
public class DriverStandingsId {
    @Embedded
    private RaceId raceId;
    @Embedded
    private DriverId driverId;

    protected DriverStandingsId() {
    }

    public DriverStandingsId(RaceId raceId, DriverId driverId) {
        this.raceId = raceId;
        this.driverId = driverId;
    }

    public RaceId raceId() {
        return raceId;
    }

    public DriverId driverId() {
        return driverId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DriverStandingsId that)) return false;
        return Objects.equals(raceId, that.raceId) && Objects.equals(driverId, that.driverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, driverId);
    }
}
