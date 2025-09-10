package no.vebb.f1.placement.collection;

import no.vebb.f1.scoring.userTables.Summary;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

import java.util.UUID;

public record PlacementObj(UUID userId, Summary summary, RaceId raceId, Year year) {
}
