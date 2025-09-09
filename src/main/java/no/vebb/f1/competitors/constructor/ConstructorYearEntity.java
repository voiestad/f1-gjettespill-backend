package no.vebb.f1.competitors.constructor;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "constructors_year")
public class ConstructorYearEntity {
    @EmbeddedId
    private ConstructorId id;

    @Column(name = "position", nullable = false)
    private int position;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "constructor_name", referencedColumnName = "constructor_name"),
            @JoinColumn(name = "year", referencedColumnName = "year")
    })
    private ConstructorColorEntity constructorColor;

    protected ConstructorYearEntity() {}

    public ConstructorYearEntity(Constructor constructorName, Year year, int position) {
        id = new ConstructorId(constructorName, year);
        this.position = position;
    }

    public Constructor constructorName() {
        return id.constructorName();
    }

    public Year year() {
        return id.year();
    }

    public int position() {
        return position;
    }

    public Color color() {
        return constructorColor == null ? null : constructorColor.color();
    }
}

