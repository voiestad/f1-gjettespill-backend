package no.vebb.f1.competitors.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "drivers_alternative_name")
public class DriverAlternativeNameEntity {
    @EmbeddedId
    private DriverAlternativeNameId id;

    @Embedded
    private Driver driverName;

    protected DriverAlternativeNameEntity() {}

    public DriverAlternativeNameEntity(String alternativeName, Year year, Driver driverName) {
        this.id = new DriverAlternativeNameId(alternativeName, year);
        this.driverName = driverName;
    }

    public String alternativeName() {
        return id.alternativeName();
    }

    public Year year() {
        return id.year();
    }

    public Driver driverName() {
        return driverName;
    }
}
