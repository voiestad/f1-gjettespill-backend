package no.vebb.f1.results.constructorStandings;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

@Embeddable
public class ConstructorStandingsId {
    @Embedded
    private RaceId raceId;
    @ManyToOne
    @JoinColumn(name = "constructor_id")
    private ConstructorEntity constructor;

    protected ConstructorStandingsId() {
    }

    public ConstructorStandingsId(RaceId raceId, ConstructorEntity constructor) {
        this.raceId = raceId;
        this.constructor = constructor;
    }

    public RaceId raceId() {
        return raceId;
    }

    public ConstructorEntity constructor() {
        return constructor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorStandingsId that)) return false;
        return Objects.equals(raceId, that.raceId) && Objects.equals(constructor, that.constructor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, constructor);
    }
}
