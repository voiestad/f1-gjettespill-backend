package no.vebb.f1.competitors.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.constructor.ConstructorId;
import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "drivers_team")
public class DriverTeamEntity {
    @EmbeddedId
    private DriverId driverId;
    @Embedded
    private ConstructorId constructorId;
    @OneToOne
    @JoinColumn(name = "driver_id", insertable = false, updatable = false)
    private DriverEntity driver;
    @OneToOne
    @JoinColumn(name = "constructor_id", insertable = false, updatable = false)
    private ConstructorEntity constructorYearEntity;

    protected DriverTeamEntity() {
    }

    public DriverTeamEntity(DriverId driverId, ConstructorId constructorId) {
        this.driverId = driverId;
        this.constructorId = constructorId;
    }

    public Year year() {
        return driver.year();
    }

    public ConstructorEntity team() {
        return constructorYearEntity;
    }
}
