package no.voiestad.f1.placement.collection;

import no.voiestad.f1.scoring.userTables.Summary;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import java.util.UUID;

public record PlacementObj(UUID userId, Summary summary, RaceId raceId, Year year) {
}
