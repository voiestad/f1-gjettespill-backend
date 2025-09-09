package no.vebb.f1.year;

import jakarta.persistence.*;

@Entity
@Table(name = "years_finished")
public class YearFinishedEntity {
    @EmbeddedId
    private Year year;

    protected YearFinishedEntity() {}

    public YearFinishedEntity(Year year) {
        this.year = year;
    }

    public Year year() {
        return year;
    }
}
