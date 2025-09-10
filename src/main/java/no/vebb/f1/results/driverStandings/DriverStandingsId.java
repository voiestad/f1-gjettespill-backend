package no.vebb.f1.results.driverStandings;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

@Embeddable
public class DriverStandingsId {
    @Embedded
    private RaceId raceId;
    @Embedded
    private Driver driverName;

    protected DriverStandingsId() {
    }

    public DriverStandingsId(RaceId raceId, Driver driverName) {
        this.raceId = raceId;
        this.driverName = driverName;
    }

    public RaceId raceId() {
        return raceId;
    }

    public Driver driverName() {
        return driverName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DriverStandingsId that)) return false;
        return raceId == that.raceId && Objects.equals(driverName, that.driverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, driverName);
    }
}
