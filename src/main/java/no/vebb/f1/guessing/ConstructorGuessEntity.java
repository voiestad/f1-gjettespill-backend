package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "constructor_guesses")
public class ConstructorGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @Column(name = "constructor_name")
    private String constructorName;

    protected ConstructorGuessEntity() {}

    public ConstructorGuessEntity(UUID userId, int position, int year, String constructorName) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.constructorName = constructorName;
    }


    public UUID userId() {
        return id.userId();
    }

    public int position() {
        return id.position();
    }

    public int year() {
        return id.year();
    }

    public String constructorName() {
        return constructorName;
    }
}
