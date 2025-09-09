package no.vebb.f1.competitors;

import jakarta.persistence.*;
import no.vebb.f1.util.domainPrimitive.Year;

@Entity
@Table(name = "drivers_team")
public class DriverTeamEntity {
    @EmbeddedId
    private DriverId id;

    @Column(name = "team", nullable = false)
    private String team;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "driver_name", referencedColumnName = "driver_name"),
            @JoinColumn(name = "year", referencedColumnName = "year")
    })
    private DriverYearEntity driverYear;

    protected DriverTeamEntity() {
    }

    public DriverTeamEntity(String driverName, Year year, String team) {
        this.id = new DriverId(driverName, year);
        this.team = team;
    }

    public String driverName() {
        return id.driverName();
    }

    public Year year() {
        return id.year();
    }

    public String team() {
        return team;
    }

}
