package no.vebb.f1.competitors.constructor;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.year.Year;

import java.util.Objects;

@Embeddable
public class ConstructorId {
    @Embedded
    private Constructor constructorName;
    @Embedded
    private Year year;

    protected ConstructorId() {}

    public ConstructorId(Constructor constructorName, Year year) {
        this.constructorName = constructorName;
        this.year = year;
    }

    public Constructor constructorName() {
        return constructorName;
    }

    public Year year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorId that)) return false;
        return year == that.year && Objects.equals(constructorName, that.constructorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constructorName, year);
    }
}
