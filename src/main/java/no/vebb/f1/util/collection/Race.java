package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;

public record Race(int position, String name, RaceId id, Year year) {
}
