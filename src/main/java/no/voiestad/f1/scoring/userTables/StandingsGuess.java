package no.voiestad.f1.scoring.userTables;

import no.voiestad.f1.scoring.domain.Diff;
import no.voiestad.f1.placement.domain.UserPoints;

public record StandingsGuess<T>(int pos, T competitor, Integer guessed, Diff diff, UserPoints points) {
}
