package no.vebb.f1.guessing;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "driver_guesses")
public class DriverGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @Embedded
    private Driver driverName;

    protected DriverGuessEntity() {}

    public DriverGuessEntity(UUID userId, int position, Year year, Driver driverName) {
        this.id = new CompetitorGuessId(userId, position, year);
        this.driverName = driverName;
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

    public Driver driverName() {
        return driverName;
    }
}
