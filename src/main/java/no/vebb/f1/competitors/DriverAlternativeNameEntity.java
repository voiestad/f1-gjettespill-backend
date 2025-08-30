package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "drivers_alternative_name")
public class DriverAlternativeNameEntity {
    @EmbeddedId
    private DriverAlternativeNameId id;

    @Column(name = "driver_name", nullable = false)
    private String driverName;

    protected DriverAlternativeNameEntity() {}

    public DriverAlternativeNameEntity(String alternativeName, int year, String driverName) {
        this.id = new DriverAlternativeNameId(alternativeName, year);
        this.driverName = driverName;
    }

    public String alternativeName() {
        return id.alternativeName();
    }

    public int year() {
        return id.year();
    }

    public String driverName() {
        return driverName;
    }
}
