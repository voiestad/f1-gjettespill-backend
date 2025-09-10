package no.vebb.f1.results.constructorStandings;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Constructor;
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

    protected ConstructorStandingsEntity() {
    }

    public ConstructorStandingsEntity(RaceId raceId, Constructor constructorName, CompetitorPosition position,
                                      CompetitorPoints points) {
        this.id = new ConstructorStandingsId(raceId, constructorName);
        this.position = position;
        this.points = points;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public Constructor constructorName() {
        return id.constructorName();
    }

    public CompetitorPosition position() {
        return position;
    }

    public CompetitorPoints points() {
        return points;
    }

}
