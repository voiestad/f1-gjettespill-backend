package no.vebb.f1.util.collection;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

public record Race(int position, String name, RaceId id, Year year) {
}
