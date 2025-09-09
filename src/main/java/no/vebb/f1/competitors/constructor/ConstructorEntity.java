package no.vebb.f1.competitors.constructor;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Constructor;

@Entity
@Table(name = "constructors")
public class ConstructorEntity {
    @EmbeddedId
    private Constructor constructorName;

    protected ConstructorEntity() {}

    public ConstructorEntity(Constructor constructorName) {
        this.constructorName = constructorName;
    }

    public Constructor constructorName() {
        return constructorName;
    }
}
