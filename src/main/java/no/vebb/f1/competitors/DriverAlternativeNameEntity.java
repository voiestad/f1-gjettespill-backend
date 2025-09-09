package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.Year;

@Entity
@Table(name = "drivers_alternative_name")
public class DriverAlternativeNameEntity {
    @EmbeddedId
    private DriverAlternativeNameId id;

    @Column(name = "driver_name", nullable = false)
    private String driverName;

    protected DriverAlternativeNameEntity() {}

    public DriverAlternativeNameEntity(String alternativeName, Year year, String driverName) {
        this.id = new DriverAlternativeNameId(alternativeName, year);
        this.driverName = driverName;
    }

    public String alternativeName() {
        return id.alternativeName();
    }

    public Year year() {
        return id.year();
    }

    public String driverName() {
        return driverName;
    }
}
