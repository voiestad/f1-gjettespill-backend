package no.voiestad.f1.guessing.constructor;

import java.util.UUID;

import no.voiestad.f1.competitors.constructor.ConstructorEntity;
import no.voiestad.f1.guessing.domain.GuessPosition;
import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

@Entity
@Table(name = "constructor_guesses")
public class ConstructorGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @ManyToOne
    @JoinColumn(name = "constructor_id")
    private ConstructorEntity constructor;

    protected ConstructorGuessEntity() {}

    public ConstructorGuessEntity(UUID userId, GuessPosition position, Year year, ConstructorEntity constructor) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.constructor = constructor;
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

    public ConstructorEntity constructor() {
        return constructor;
    }
}
