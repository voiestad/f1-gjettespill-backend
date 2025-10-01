package no.vebb.f1.collection;

import no.vebb.f1.competitors.domain.CompetitorName;
import no.vebb.f1.competitors.domain.ConstructorName;
import no.vebb.f1.competitors.domain.DriverName;
import no.vebb.f1.guessing.constructor.ConstructorGuessEntity;
import no.vebb.f1.guessing.driver.DriverGuessEntity;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.year.Year;

public record CompetitorGuessYear<T extends CompetitorName>(GuessPosition position, T competitor, Year year) {
    public static CompetitorGuessYear<ConstructorName> fromEntity(ConstructorGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.constructor().constructorName(), entity.year());
    }
    public static CompetitorGuessYear<DriverName> fromEntity(DriverGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.driver().driverName(), entity.year());
    }
}
