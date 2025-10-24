package no.voiestad.f1.scoring.userTables;

import no.voiestad.f1.scoring.domain.Diff;
import no.voiestad.f1.stats.domain.Flag;
import no.voiestad.f1.placement.domain.UserPoints;

public record FlagGuess(Flag flag, int guessed, int actual, Diff diff, UserPoints points) {
}
