package no.vebb.f1.competitors.driver;

import no.vebb.f1.competitors.constructor.ConstructorDTO;
import no.vebb.f1.competitors.domain.Driver;

public record DriverDTO(DriverId id, Driver name, ConstructorDTO team) {
    public static DriverDTO fromEntity(DriverEntity driverEntity) {
        return new DriverDTO(driverEntity.driverId(), driverEntity.driverName(), null);
    }

    public static DriverDTO fromEntityWithTeam(DriverEntity driverEntity) {
        return new DriverDTO(driverEntity.driverId(), driverEntity.driverName(), ConstructorDTO.fromEntity(driverEntity.team()));
    }
}
