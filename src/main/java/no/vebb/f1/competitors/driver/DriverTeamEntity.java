package no.vebb.f1.competitors.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "drivers_team")
public class DriverTeamEntity {
    @EmbeddedId
    private DriverId id;

    @Embedded
    private Constructor team;

    protected DriverTeamEntity() {
    }

    public DriverTeamEntity(Driver driverName, Year year, Constructor team) {
        this.id = new DriverId(driverName, year);
        this.team = team;
    }

    public Driver driverName() {
        return id.driverName();
    }

    public Year year() {
        return id.year();
    }

    public Constructor team() {
        return team;
    }

}
