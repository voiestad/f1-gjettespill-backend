package no.voiestad.f1.guessing.flag;

import java.util.UUID;

import no.voiestad.f1.stats.domain.Flag;
import no.voiestad.f1.year.Year;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "flag_guesses")
public class FlagGuessEntity {
    @EmbeddedId
    private FlagGuessId id;

    @Column(name = "amount", nullable = false)
    private int amount;

    protected FlagGuessEntity() {}

    public FlagGuessEntity(UUID userId, Flag flagName, Year year, int amount) {
        this.id = new FlagGuessId(userId, flagName, year);
        this.amount = amount;
    }

    public UUID userId() {
        return id.userId();
    }

    public Flag flagName() {
        return id.flagName();
    }

    public Year year() {
        return id.year();
    }

    public int amount() {
        return amount;
    }
}
