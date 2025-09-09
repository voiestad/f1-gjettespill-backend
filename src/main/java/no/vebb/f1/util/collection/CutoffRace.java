package no.vebb.f1.util.collection;

import java.time.LocalDateTime;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;

public record CutoffRace(int position, String name, RaceId id, LocalDateTime cutoff, Year year) {

}
