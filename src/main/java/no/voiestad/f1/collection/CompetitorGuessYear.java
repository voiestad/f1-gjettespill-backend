package no.voiestad.f1.collection;

import no.voiestad.f1.competitors.domain.CompetitorName;
import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.guessing.constructor.ConstructorGuessEntity;
import no.voiestad.f1.guessing.driver.DriverGuessEntity;
import no.voiestad.f1.guessing.domain.GuessPosition;
import no.voiestad.f1.year.Year;

public record CompetitorGuessYear<T extends CompetitorName>(GuessPosition position, T competitor, Year year) {
    public static CompetitorGuessYear<ConstructorName> fromEntity(ConstructorGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.constructor().constructorName(), entity.year());
    }
    public static CompetitorGuessYear<DriverName> fromEntity(DriverGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.driver().driverName(), entity.year());
    }
}
