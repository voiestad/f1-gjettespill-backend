package no.vebb.f1.scoring.userTables;

import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.placement.domain.UserPoints;

public record FlagGuess(Flag flag, int guessed, int actual, Diff diff, UserPoints points) {
}
