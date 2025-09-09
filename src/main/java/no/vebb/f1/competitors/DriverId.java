package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.Objects;

@Embeddable
public class DriverId {
    @Column(name = "driver_name", nullable = false)
    private String driverName;
    @Embedded
    private Year year;

    protected DriverId() {}

    public DriverId(String driverName, Year year) {
        this.driverName = driverName;
        this.year = year;
    }

    public String driverName() {
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
