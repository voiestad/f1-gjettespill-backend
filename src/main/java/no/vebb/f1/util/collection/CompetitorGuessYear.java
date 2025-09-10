package no.vebb.f1.util.collection;

import no.vebb.f1.competitors.domain.Competitor;
import no.vebb.f1.guessing.constructor.ConstructorGuessEntity;
import no.vebb.f1.guessing.driver.DriverGuessEntity;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.year.Year;

public record CompetitorGuessYear<T extends Competitor>(GuessPosition position, T competitor, Year year) {
    public static CompetitorGuessYear<Constructor> fromEntity(ConstructorGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.constructorName(), entity.year());
    }
    public static CompetitorGuessYear<Driver> fromEntity(DriverGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.driverName(), entity.year());
    }
}
