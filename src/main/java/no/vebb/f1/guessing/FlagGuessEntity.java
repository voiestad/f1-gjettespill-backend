package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "flag_guesses")
public class FlagGuessEntity {
    @EmbeddedId
    private FlagGuessId id;

    @Column(name = "amount", nullable = false)
    private int amount;

    protected FlagGuessEntity() {}

    public FlagGuessEntity(UUID userId, String flagName, Year year, int amount) {
        this.id = new FlagGuessId(userId, flagName, year);
        this.amount = amount;
    }

    public UUID userId() {
        return id.userId();
    }

    public String flagName() {
        return id.flagName();
    }

    public Year year() {
        return id.year();
    }

    public int amount() {
        return amount;
    }
}
