package no.vebb.f1.scoring.userTables;

import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.placement.domain.UserPoints;

public record StandingsGuess<T>(int pos, T competitor, Integer guessed, Diff diff, UserPoints points) {
}
