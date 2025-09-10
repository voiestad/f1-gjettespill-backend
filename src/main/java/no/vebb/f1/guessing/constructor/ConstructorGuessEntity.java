package no.vebb.f1.guessing.constructor;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "constructor_guesses")
public class ConstructorGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @Embedded
    private Constructor constructorName;

    protected ConstructorGuessEntity() {}

    public ConstructorGuessEntity(UUID userId, GuessPosition position, Year year, Constructor constructorName) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.constructorName = constructorName;
    }


    public UUID userId() {
        return id.userId();
    }

    public GuessPosition position() {
        return id.position();
    }

    public Year year() {
        return id.year();
    }

    public Constructor constructorName() {
        return constructorName;
    }
}
