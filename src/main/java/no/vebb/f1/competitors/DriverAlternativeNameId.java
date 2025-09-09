package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.Objects;

@Embeddable
public class DriverAlternativeNameId {
    @Column(name = "alternative_name", nullable = false)
    private String alternativeName;

    @Embedded
    private Year year;

    protected DriverAlternativeNameId() {}

    public DriverAlternativeNameId(String alternativeName, Year year) {
        this.alternativeName = alternativeName;
        this.year = year;
    }

    public String alternativeName() {
        return alternativeName;
    }

    public Year year() {
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
