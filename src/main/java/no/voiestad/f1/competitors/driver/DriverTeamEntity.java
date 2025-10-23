package no.voiestad.f1.competitors.driver;

import no.voiestad.f1.competitors.constructor.ConstructorEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "drivers_team")
public class DriverTeamEntity {
    @EmbeddedId
    private DriverId driverId;
    @OneToOne
    @JoinColumn(name = "constructor_id")
    private ConstructorEntity constructorEntity;

    protected DriverTeamEntity() {
    }

    public DriverTeamEntity(DriverId driverId, ConstructorEntity constructorEntity) {
        this.driverId = driverId;
        this.constructorEntity = constructorEntity;
    }

    public ConstructorEntity team() {
        return constructorEntity;
    }
}
