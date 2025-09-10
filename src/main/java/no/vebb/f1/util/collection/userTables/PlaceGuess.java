package no.vebb.f1.util.collection.userTables;

import no.vebb.f1.race.RacePosition;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.placement.domain.UserPoints;

public record PlaceGuess(RacePosition racePos, String raceName, String driver, int startPos, int finishPos, Diff diff,
                         UserPoints points) {
}
