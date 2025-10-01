package no.vebb.f1.guessing.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.guessing.constructor.CompetitorGuessId;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "driver_guesses")
public class DriverGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;

    protected DriverGuessEntity() {}

    public DriverGuessEntity(UUID userId, GuessPosition position, Year year, DriverEntity driver) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.driver = driver;
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

    public DriverEntity driver() {
        return driver;
    }
}
