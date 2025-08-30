package no.vebb.f1.cutoff;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "year_cutoffs")
public class YearCutoffEntity {
    @Id
    @Column(name = "year")
    private int year;

    @Column(name = "cutoff", nullable = false)
    private Instant cutoff;

    protected YearCutoffEntity() {}

    public YearCutoffEntity(int year, Instant cutoff) {
        this.year = year;
        this.cutoff = cutoff;
    }

    public int year() {
        return year;
    }

    public Instant cutoff() {
        return cutoff;
    }
}
