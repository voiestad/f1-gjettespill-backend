package no.vebb.f1.guessing.constructor;

import jakarta.persistence.*;
import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.constructor.ConstructorId;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "constructor_guesses")
public class ConstructorGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @Embedded
    private ConstructorId constructorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "constructor_id", insertable = false, updatable = false)
    private ConstructorEntity constructor;

    protected ConstructorGuessEntity() {}

    public ConstructorGuessEntity(UUID userId, GuessPosition position, Year year, ConstructorId constructorId) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.constructorId = constructorId;
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
