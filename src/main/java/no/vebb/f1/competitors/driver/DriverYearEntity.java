package no.vebb.f1.competitors.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "drivers_year")
public class DriverYearEntity {
    @EmbeddedId
    private DriverId id;

    @Column(name =  "position", nullable = false)
    private int position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "driver_name", referencedColumnName = "driver_name"),
            @JoinColumn(name = "year", referencedColumnName = "year")
    })
    private DriverTeamEntity driverTeam;

    protected DriverYearEntity() {}

    public DriverYearEntity(Driver driverName, Year year, int position) {
        this.id = new DriverId(driverName, year);
        this.position = position;
    }

    public Driver driverName() {
        return id.driverName();
    }

    public Year year() {
        return id.year();
    }

    public int position() {
        return position;
    }

    public Constructor team() {
        return driverTeam == null ? null : driverTeam.team();
    }
}
