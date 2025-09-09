package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.Year;

@Entity
@Table(name = "drivers_year")
public class DriverYearEntity {
    @EmbeddedId
    private DriverId id;

    @Column(name =  "position", nullable = false)
    private int position;

    protected DriverYearEntity() {}

    public DriverYearEntity(String driverName, Year year, int position) {
        this.id = new DriverId(driverName, year);
        this.position = position;
    }

    public String driverName() {
        return id.driverName();
    }

    public Year year() {
        return id.year();
    }

    public int position() {
        return position;
    }
}
