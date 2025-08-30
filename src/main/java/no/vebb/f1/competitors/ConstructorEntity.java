package no.vebb.f1.competitors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "constructors")
public class ConstructorEntity {
    @Id
    @Column(name = "constructor_name")
    private String constructorName;

    protected ConstructorEntity() {}

    public ConstructorEntity(String constructorName) {
        this.constructorName = constructorName;
    }

    public String constructorName() {
        return constructorName;
    }
}
