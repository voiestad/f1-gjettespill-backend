package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.UUID;

@Entity
@Table(name = "constructor_guesses")
public class ConstructorGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @Column(name = "constructor_name")
    private String constructorName;

    protected ConstructorGuessEntity() {}

    public ConstructorGuessEntity(UUID userId, int position, Year year, String constructorName) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.constructorName = constructorName;
    }


    public UUID userId() {
        return id.userId();
    }

    public int position() {
        return id.position();
    }

    public Year year() {
        return id.year();
    }

    public String constructorName() {
        return constructorName;
    }
}
