package no.vebb.f1.util.collection;

import no.vebb.f1.guessing.ConstructorGuessEntity;
import no.vebb.f1.guessing.DriverGuessEntity;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;
import no.vebb.f1.util.domainPrimitive.Year;

public record CompetitorGuessYear<T>(int position, T competitor, Year year) {
    public static CompetitorGuessYear<Constructor> fromEntity(ConstructorGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), new Constructor(entity.constructorName()), entity.year());
    }
    public static CompetitorGuessYear<Driver> fromEntity(DriverGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), new Driver(entity.driverName()), entity.year());
    }
}
