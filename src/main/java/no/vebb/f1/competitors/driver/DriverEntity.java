package no.vebb.f1.competitors.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;

@Entity
@Table(name = "drivers")
public class DriverEntity {
    @EmbeddedId
    private Driver driverName;

    protected DriverEntity() {}

    public DriverEntity(Driver driverName) {
        this.driverName = driverName;
    }

    public Driver driverName() {
        return driverName;
    }
}
