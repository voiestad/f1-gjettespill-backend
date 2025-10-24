package no.voiestad.f1.cutoff;

import java.time.Instant;

import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

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
