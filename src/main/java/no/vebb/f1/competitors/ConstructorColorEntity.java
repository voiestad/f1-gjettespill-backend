package no.vebb.f1.competitors;

import jakarta.persistence.*;

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

    public ConstructorColorEntity(String constructorName, int year, String color) {
        this.id = new ConstructorId(constructorName, year);
        this.color = color;
    }

    public String constructorName() {
        return id.constructorName();
    }

    public int year() {
        return id.year();
    }

    public String color() {
        return color;
    }


}
