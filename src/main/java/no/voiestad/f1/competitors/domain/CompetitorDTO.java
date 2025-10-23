package no.voiestad.f1.competitors.domain;

import no.voiestad.f1.competitors.constructor.ConstructorEntity;
import no.voiestad.f1.competitors.driver.DriverEntity;

public record CompetitorDTO(CompetitorName name, CompetitorId id, Color color) {
    public static CompetitorDTO fromEntity(DriverEntity driverEntity) {
        return new CompetitorDTO(driverEntity.driverName(), driverEntity.driverId(), driverEntity.team().color());
    }
    public static CompetitorDTO fromEntity(ConstructorEntity constructorEntity) {
        return new CompetitorDTO(constructorEntity.constructorName(), constructorEntity.constructorId(), constructorEntity.color());
    }
}
