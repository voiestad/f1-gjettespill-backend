package no.voiestad.f1.competitors.driver;

import java.util.Objects;

import no.voiestad.f1.competitors.domain.CompetitorId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import com.fasterxml.jackson.annotation.JsonValue;

@Embeddable
public class DriverId implements CompetitorId {
    @Column(name = "driver_id", nullable = false)
    private int value;

    protected DriverId() {}

    public DriverId(int value) {
        this.value = value;
    }

    @JsonValue
    public int toValue() {
        return value;
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
