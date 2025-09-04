package no.vebb.f1.placement;

import no.vebb.f1.util.collection.userTables.Summary;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.UUID;

public record PlacementObj(UUID userId, Summary summary, RaceId raceId, Year year) {
}
