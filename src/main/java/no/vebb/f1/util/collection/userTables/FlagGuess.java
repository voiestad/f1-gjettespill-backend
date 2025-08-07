package no.vebb.f1.util.collection.userTables;

import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.Points;

public record FlagGuess(Flag flag, int guessed, int actual, Diff diff, Points points) {
}
