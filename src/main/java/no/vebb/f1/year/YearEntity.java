package no.vebb.f1.year;

import jakarta.persistence.*;
import no.vebb.f1.util.domainPrimitive.Year;

@Entity
@Table(name = "years")
public class YearEntity {
    @EmbeddedId
    private Year year;

    protected YearEntity() {}

    public YearEntity(int year) {
        this.year = new Year(year);
    }

    public Year year() {
        return year;
    }
}
