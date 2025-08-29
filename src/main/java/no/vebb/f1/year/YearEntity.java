package no.vebb.f1.year;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "years")
public class YearEntity {
    @Id
    @Column(name = "year")
    private int year;

    protected YearEntity() {}

    public YearEntity(int year) {
        this.year = year;
    }

    public int year() {
        return year;
    }
}
