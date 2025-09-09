package no.vebb.f1.competitors;

import jakarta.persistence.*;
import no.vebb.f1.util.domainPrimitive.Year;

@Entity
@Table(name = "constructors_color")
public class ConstructorColorEntity {
    @EmbeddedId
    private ConstructorId id;

    @Column(name = "color", nullable = false)
    private String color;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "constructor_name", referencedColumnName = "constructor_name"),
            @JoinColumn(name = "year", referencedColumnName = "year")
    })
    private ConstructorYearEntity constructorYear;

    protected ConstructorColorEntity() {}

    public ConstructorColorEntity(String constructorName, Year year, String color) {
        this.id = new ConstructorId(constructorName, year);
        this.color = color;
    }

    public String constructorName() {
        return id.constructorName();
    }

    public Year year() {
        return id.year();
    }

    public String color() {
        return color;
    }


}
