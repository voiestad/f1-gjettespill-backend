package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DriverId {
    @Column(name = "driver_name", nullable = false)
    private String driverName;
    @Column(name = "year", nullable = false)
    private int year;

    protected DriverId() {}

    public DriverId(String driverName, int year) {
        this.driverName = driverName;
        this.year = year;
    }

    public String driverName() {
        return driverName;
    }

    public int year() {
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
