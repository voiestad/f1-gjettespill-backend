package no.vebb.f1.results.constructorStandings;

import jakarta.persistence.*;
import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.constructor.ConstructorId;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.domain.CompetitorPoints;
import no.vebb.f1.results.domain.CompetitorPosition;

@Entity
@Table(name = "constructor_standings")
public class ConstructorStandingsEntity {
    @EmbeddedId
    private ConstructorStandingsId id;
    @Embedded
    private CompetitorPosition position;
    @Embedded
    private CompetitorPoints points;
    @ManyToOne
    @JoinColumn(name = "constructor_id", insertable = false, updatable = false)
    private ConstructorEntity constructor;

    protected ConstructorStandingsEntity() {
    }

    public ConstructorStandingsEntity(RaceId raceId, ConstructorId constructorId, CompetitorPosition position,
                                      CompetitorPoints points) {
        this.id = new ConstructorStandingsId(raceId, constructorId);
        this.position = position;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public ConstructorEntity constructor() {
        return constructor;
    }

    public CompetitorPosition position() {
        return position;
    }

    public CompetitorPoints points() {
        return points;
    }

}
