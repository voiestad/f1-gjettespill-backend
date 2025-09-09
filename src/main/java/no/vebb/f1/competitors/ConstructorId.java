package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.Objects;

@Embeddable
public class ConstructorId {
    @Column(name = "constructor_name", nullable = false)
    private String constructorName;
    @Embedded
    private Year year;

    protected ConstructorId() {}

    public ConstructorId(String constructorName, Year year) {
        this.constructorName = constructorName;
        this.year = year;
    }

    public String constructorName() {
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
