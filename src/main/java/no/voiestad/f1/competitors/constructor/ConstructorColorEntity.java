package no.voiestad.f1.competitors.constructor;

import no.voiestad.f1.competitors.domain.Color;

import jakarta.persistence.*;

@Entity
@Table(name = "constructors_color")
public class ConstructorColorEntity {
    @EmbeddedId
    private ConstructorId constructorId;

    @Embedded
    private Color color;

    protected ConstructorColorEntity() {}

    public ConstructorColorEntity(ConstructorId constructorId, Color color) {
        this.constructorId = constructorId;
        this.color = color;
    }

    public Color color() {
        return color;
    }


}
