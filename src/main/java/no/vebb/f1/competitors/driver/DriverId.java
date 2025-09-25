package no.vebb.f1.competitors.driver;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DriverId {
    @Column(name = "driver_id", nullable = false)
    private int value;

    protected DriverId() {}

    public DriverId(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DriverId driverId)) return false;
        return value == driverId.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
