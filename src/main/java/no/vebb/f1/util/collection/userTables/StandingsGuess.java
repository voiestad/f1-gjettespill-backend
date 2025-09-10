package no.vebb.f1.util.collection.userTables;

import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.placement.domain.UserPoints;

public record StandingsGuess<T>(int pos, T competitor, Integer guessed, Diff diff, UserPoints points) {
}
