package no.voiestad.f1.year;

import jakarta.persistence.*;

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
