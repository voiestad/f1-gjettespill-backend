package no.vebb.f1.collection;

import no.vebb.f1.guessing.flag.FlagGuessEntity;
import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.year.Year;

public record FlagGuessYear(Flag flag, int guessed, Year year) {
    public static FlagGuessYear fromEntity(FlagGuessEntity entity) {
        return new FlagGuessYear(entity.flagName(), entity.amount(), entity.year());
    }
}
