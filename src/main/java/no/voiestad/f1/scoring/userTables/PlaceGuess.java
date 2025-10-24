package no.voiestad.f1.scoring.userTables;

import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.results.domain.CompetitorPosition;
import no.voiestad.f1.scoring.domain.Diff;
import no.voiestad.f1.placement.domain.UserPoints;

public record PlaceGuess(RacePosition racePos, String raceName, DriverName driver, CompetitorPosition startPos,
                         CompetitorPosition finishPos, Diff diff, UserPoints points) {
}
