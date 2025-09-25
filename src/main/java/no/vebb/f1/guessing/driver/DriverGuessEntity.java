package no.vebb.f1.guessing.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.driver.DriverEntity;
import no.vebb.f1.competitors.driver.DriverId;
import no.vebb.f1.guessing.domain.GuessPosition;
import no.vebb.f1.guessing.constructor.CompetitorGuessId;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "driver_guesses")
public class DriverGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @Embedded
    private DriverId driverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", insertable = false, updatable = false)
    private DriverEntity driver;

    protected DriverGuessEntity() {}

    public DriverGuessEntity(UUID userId, GuessPosition position, Year year, DriverId driverId) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.driverId = driverId;
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
