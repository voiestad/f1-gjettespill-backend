package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "drivers")
public class DriverEntity {
    @Id
    @Column(name = "driver_name")
    private String driverName;

    protected DriverEntity() {}

    public DriverEntity(String driverName) {
        this.driverName = driverName;
    }

    public String driverName() {
        return driverName;
    }
}
