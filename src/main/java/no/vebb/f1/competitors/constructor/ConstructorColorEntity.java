package no.vebb.f1.competitors.constructor;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "constructors_color")
public class ConstructorColorEntity {
    @EmbeddedId
    private ConstructorId id;

    @Embedded
    private Color color;

    protected ConstructorColorEntity() {}

    public ConstructorColorEntity(Constructor constructorName, Year year, Color color) {
        this.id = new ConstructorId(constructorName, year);
        this.color = color;
    }

    public Constructor constructorName() {
        return id.constructorName();
    }

    public Year year() {
        return id.year();
    }

    public Color color() {
        return color;
    }


}
