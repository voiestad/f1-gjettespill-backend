package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ConstructorId {
    @Column(name = "constructor_name", nullable = false)
    private String constructorName;
    @Column(name = "year", nullable = false)
    private int year;

    protected ConstructorId() {}

    public ConstructorId(String constructorName, int year) {
        this.constructorName = constructorName;
        this.year = year;
    }

    public String constructorName() {
        return constructorName;
    }

    public int year() {
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
