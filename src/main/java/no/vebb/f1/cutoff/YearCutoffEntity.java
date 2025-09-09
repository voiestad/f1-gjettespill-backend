package no.vebb.f1.cutoff;

import jakarta.persistence.*;
import no.vebb.f1.util.domainPrimitive.Year;

import java.time.Instant;

@Entity
@Table(name = "year_cutoffs")
public class YearCutoffEntity {
    @EmbeddedId
    private Year year;

    @Column(name = "cutoff", nullable = false)
    private Instant cutoff;

    protected YearCutoffEntity() {}

    public YearCutoffEntity(Year year, Instant cutoff) {
        this.year = year;
        this.cutoff = cutoff;
    }

    public Year year() {
        return year;
    }

    public Instant cutoff() {
        return cutoff;
    }
}
