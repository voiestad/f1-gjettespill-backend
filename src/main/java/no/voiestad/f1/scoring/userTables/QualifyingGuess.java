package no.voiestad.f1.scoring.userTables;

import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.placement.domain.UserPoints;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.results.domain.CompetitorPosition;
import no.voiestad.f1.scoring.domain.Diff;

public record QualifyingGuess(RacePosition racePos, String raceName, DriverName driver, CompetitorPosition qualifyingPos,
                              Diff diff, UserPoints points) {
}
