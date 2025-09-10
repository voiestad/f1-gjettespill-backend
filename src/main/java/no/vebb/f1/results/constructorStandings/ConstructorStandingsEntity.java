package no.vebb.f1.results.constructorStandings;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.race.RaceId;

@Entity
@Table(name = "constructor_standings")
public class ConstructorStandingsEntity {
    @EmbeddedId
    private ConstructorStandingsId id;
    @Column(name = "position", nullable = false)
    private int position;
    @Column(name = "points", nullable = false)
    private int points;

    protected ConstructorStandingsEntity() {
    }

    public ConstructorStandingsEntity(RaceId raceId, Constructor constructorName, int position, int points) {
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

    public int position() {
        return position;
    }

    public int points() {
        return points;
    }

}
