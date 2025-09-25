package no.vebb.f1.collection;

import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.domain.Competitor;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.guessing.constructor.ConstructorGuessEntity;
import no.vebb.f1.guessing.driver.DriverGuessEntity;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.year.Year;

public record CompetitorGuessYear<T extends Competitor>(GuessPosition position, T competitor, Year year) {
    public static CompetitorGuessYear<ConstructorEntity> fromEntity(ConstructorGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.constructor(), entity.year());
    }
    public static CompetitorGuessYear<DriverEntity> fromEntity(DriverGuessEntity entity) {
        return new CompetitorGuessYear<>(entity.position(), entity.driver(), entity.year());
    }
}
