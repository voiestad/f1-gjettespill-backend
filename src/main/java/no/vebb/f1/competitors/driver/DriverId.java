package no.vebb.f1.competitors.driver;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;

import java.util.Objects;

@Embeddable
public class DriverId {
    @Embedded
    private Driver driverName;
    @Embedded
    private Year year;

    protected DriverId() {}

    public DriverId(Driver driverName, Year year) {
        this.driverName = driverName;
        this.year = year;
    }

    public Driver driverName() {
        return driverName;
    }

    public Year year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DriverId driverId)) return false;
        return year == driverId.year && Objects.equals(driverName, driverId.driverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverName, year);
    }
}
