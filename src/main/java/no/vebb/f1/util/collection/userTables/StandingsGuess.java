package no.vebb.f1.util.collection.userTables;

import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;

public record StandingsGuess<T>(int pos, T competitor, Integer guessed, Diff diff, Points points) {
}
