package no.voiestad.f1.guessing.driver;

import java.util.UUID;

import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.guessing.domain.GuessPosition;
import no.voiestad.f1.guessing.constructor.CompetitorGuessId;
import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

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
