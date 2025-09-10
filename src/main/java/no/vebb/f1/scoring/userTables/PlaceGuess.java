package no.vebb.f1.scoring.userTables;

import no.vebb.f1.race.RacePosition;
import no.vebb.f1.results.domain.CompetitorPosition;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.placement.domain.UserPoints;

public record PlaceGuess(RacePosition racePos, String raceName, String driver, CompetitorPosition startPos,
                         CompetitorPosition finishPos, Diff diff, UserPoints points) {
}
