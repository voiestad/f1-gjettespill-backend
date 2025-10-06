package no.vebb.f1.competitors.constructor;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.ConstructorName;
import no.vebb.f1.year.Year;

import java.util.Objects;

@Entity
@Table(name = "constructors")
public class ConstructorEntity {
    @EmbeddedId
    private ConstructorId constructorId;
    @Embedded
    private ConstructorName constructorName;
    @Embedded
    private Year year;
    @Column(name = "position", nullable = false)
    private int position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "constructor_id", insertable = false, updatable = false)
    private ConstructorColorEntity constructorColor;

    protected ConstructorEntity() {}

    public ConstructorEntity(ConstructorId constructorId, ConstructorName constructorName, Year year, int position){
        this.constructorId = constructorId;
        this.constructorName = constructorName;
        this.year = year;
        this.position = position;
    }

    public ConstructorId constructorId() {
        return constructorId;
    }

    public ConstructorName constructorName() {
        return constructorName;
    }

    public Year year() {
        return year;
    }

    public int position() {
        return position;
    }

    public Color color() {
        return constructorColor == null ? null : constructorColor.color();
    }

    public ConstructorEntity withPosition(int newPosition) {
        return new ConstructorEntity(constructorId, constructorName, year, newPosition);
    }

    public ConstructorEntity withName(ConstructorName newName) {
        return new ConstructorEntity(constructorId, newName, year, position);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorEntity that)) return false;
        return Objects.equals(position, that.position) && Objects.equals(constructorId, that.constructorId) && Objects.equals(constructorName, that.constructorName) && Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constructorId, constructorName, year, position);
    }
}

