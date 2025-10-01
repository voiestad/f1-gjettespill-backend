package no.vebb.f1.competitors.constructor;

import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.ConstructorName;

public record ConstructorDTO(ConstructorId id, ConstructorName name, Color color) {
    public static ConstructorDTO fromEntity(ConstructorEntity constructorEntity) {
        if (constructorEntity == null) {
            return null;
        }
        return new ConstructorDTO(constructorEntity.constructorId(), constructorEntity.constructorName(), null);
    }

    public static ConstructorDTO fromEntityWithColor(ConstructorEntity constructorEntity) {
        if (constructorEntity == null) {
            return null;
        }
        return new ConstructorDTO(constructorEntity.constructorId(), constructorEntity.constructorName(), constructorEntity.color());
    }
}
