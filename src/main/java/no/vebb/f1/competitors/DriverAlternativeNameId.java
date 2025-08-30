package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DriverAlternativeNameId {
    @Column(name = "alternative_name", nullable = false)
    private String alternativeName;

    @Column(name = "year", nullable = false)
    private int year;

    protected DriverAlternativeNameId() {}

    public DriverAlternativeNameId(String alternativeName, int year) {
        this.alternativeName = alternativeName;
        this.year = year;
    }

    public String alternativeName() {
        return alternativeName;
    }

    public int year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DriverAlternativeNameId that)) return false;
        return year == that.year && Objects.equals(alternativeName, that.alternativeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alternativeName, year);
    }
}
