package no.vebb.f1.results.constructorStandings;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.competitors.constructor.ConstructorId;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

@Embeddable
public class ConstructorStandingsId {
    @Embedded
    private RaceId raceId;
    @Embedded
    private ConstructorId constructorId;

    protected ConstructorStandingsId() {
    }

    public ConstructorStandingsId(RaceId raceId, ConstructorId constructorId) {
        this.raceId = raceId;
        this.constructorId = constructorId;
    }

    public RaceId raceId() {
        return raceId;
    }

    public ConstructorId constructorId() {
        return constructorId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorStandingsId that)) return false;
        return raceId == that.raceId && Objects.equals(constructorId, that.constructorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, constructorId);
    }
}
