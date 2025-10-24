package no.voiestad.f1.collection;

import no.voiestad.f1.race.RaceEntity;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.year.Year;

public record Race(RacePosition position, String name, RaceId id, Year year) {
    public static Race fromEntity(RaceEntity entity) {
        return new Race(entity.position(), entity.name(), entity.raceId(), entity.year());
    }
}
