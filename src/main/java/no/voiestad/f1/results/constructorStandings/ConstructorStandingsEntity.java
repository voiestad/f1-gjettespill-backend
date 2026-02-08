package no.voiestad.f1.results.constructorStandings;

import no.voiestad.f1.competitors.constructor.ConstructorEntity;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.results.domain.CompetitorPosition;

import jakarta.persistence.*;

@Entity
@Table(name = "constructor_standings")
public class ConstructorStandingsEntity {
    @EmbeddedId
    private ConstructorStandingsId id;
    @Embedded
    private CompetitorPosition position;

    protected ConstructorStandingsEntity() {
    }

    public ConstructorStandingsEntity(RaceId raceId, ConstructorEntity constructor, CompetitorPosition position) {
        this.id = new ConstructorStandingsId(raceId, constructor);
        this.position = position;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public ConstructorEntity constructor() {
        return id.constructor();
    }

    public CompetitorPosition position() {
        return position;
    }

}
