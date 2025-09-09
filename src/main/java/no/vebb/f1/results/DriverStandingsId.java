package no.vebb.f1.results;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.util.domainPrimitive.RaceId;

import java.util.Objects;

@Embeddable
public class DriverStandingsId {
    @Embedded
    private RaceId raceId;
    @Column(name = "driver_name", nullable = false)
    private String driverName;

    protected DriverStandingsId() {
    }

    public DriverStandingsId(RaceId raceId, String driverName) {
        this.raceId = raceId;
        this.driverName = driverName;
    }

    public RaceId raceId() {
        return raceId;
    }

    public String driverName() {
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
