package no.vebb.f1.results;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DriverStandingsId {
    @Column(name = "race_id", nullable = false)
    private int raceId;
    @Column(name = "driver_name", nullable = false)
    private String driverName;

    protected DriverStandingsId() {
    }

    public DriverStandingsId(int raceId, String driverName) {
        this.raceId = raceId;
        this.driverName = driverName;
    }

    public int raceId() {
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
