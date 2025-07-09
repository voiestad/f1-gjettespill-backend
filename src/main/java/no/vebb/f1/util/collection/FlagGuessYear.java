package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.Year;

public record FlagGuessYear(Flag flag, int guessed, Year year) {
}
