package no.vebb.f1.collection;

import no.vebb.f1.race.RaceEntity;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.race.RacePosition;
import no.vebb.f1.year.Year;

public record Race(RacePosition position, String name, RaceId id, Year year) {
    public static Race fromEntity(RaceEntity entity) {
        return new Race(entity.position(), entity.name(), entity.raceId(), entity.year());
    }
}
