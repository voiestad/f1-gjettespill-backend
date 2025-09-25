package no.vebb.f1.results.startingGrid;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.competitors.driver.DriverId;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

@Embeddable
public class StartingGridId {
    @Embedded
    private RaceId raceId;
    @Embedded
    private DriverId driverId;

    protected StartingGridId() {}

    public StartingGridId(RaceId raceId, DriverId driverId) {
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
        if (!(o instanceof StartingGridId that)) return false;
        return raceId == that.raceId && Objects.equals(driverId, that.driverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, driverId);
    }
}
