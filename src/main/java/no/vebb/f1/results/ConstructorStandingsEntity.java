package no.vebb.f1.results;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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

    public ConstructorStandingsEntity(int raceId, String constructorName, int position, int points) {
        this.id = new ConstructorStandingsId(raceId, constructorName);
        this.position = position;
        this.points = points;
    }

    public int raceId() {
        return id.raceId();
    }

    public String constructorName() {
        return id.constructorName();
    }

    public int position() {
        return position;
    }

    public int points() {
        return points;
    }

}
