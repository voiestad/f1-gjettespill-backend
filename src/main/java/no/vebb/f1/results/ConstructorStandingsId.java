package no.vebb.f1.results;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ConstructorStandingsId {
    @Column(name = "race_id", nullable = false)
    private int raceId;
    @Column(name = "constructor_name", nullable = false)
    private String constructorName;

    protected ConstructorStandingsId() {
    }

    public ConstructorStandingsId(int raceId, String constructorName) {
        this.raceId = raceId;
        this.constructorName = constructorName;
    }

    public int raceId() {
        return raceId;
    }

    public String constructorName() {
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
