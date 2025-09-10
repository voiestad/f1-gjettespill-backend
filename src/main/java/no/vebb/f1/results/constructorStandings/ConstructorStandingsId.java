package no.vebb.f1.results.constructorStandings;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

@Embeddable
public class ConstructorStandingsId {
    @Embedded
    private RaceId raceId;
    @Embedded
    private Constructor constructorName;

    protected ConstructorStandingsId() {
    }

    public ConstructorStandingsId(RaceId raceId, Constructor constructorName) {
        this.raceId = raceId;
        this.constructorName = constructorName;
    }

    public RaceId raceId() {
        return raceId;
    }

    public Constructor constructorName() {
        return constructorName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorStandingsId that)) return false;
        return raceId == that.raceId && Objects.equals(constructorName, that.constructorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, constructorName);
    }
}
