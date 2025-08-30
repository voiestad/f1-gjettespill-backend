package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "constructors_year")
public class ConstructorYearEntity {
    @EmbeddedId
    private ConstructorId id;

    @Column(name = "position", nullable = false)
    private int position;

    protected ConstructorYearEntity() {}

    public ConstructorYearEntity(String constructorName, int year, int position) {
        id = new ConstructorId(constructorName, year);
        this.position = position;
    }

    public String constructorName() {
        return id.constructorName();
    }

    public int year() {
        return id.year();
    }

    public int position() {
        return position;
    }
}

