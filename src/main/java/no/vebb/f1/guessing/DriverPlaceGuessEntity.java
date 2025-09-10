package no.vebb.f1.guessing;

import jakarta.persistence.*;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.race.RaceId;

import java.util.UUID;

@Entity
@Table(name = "driver_place_guesses")
public class DriverPlaceGuessEntity {
    @EmbeddedId
    private DriverPlaceGuessId id;

    @Embedded
    private Driver driverName;

    protected DriverPlaceGuessEntity() {}

    public DriverPlaceGuessEntity(UUID userId, RaceId raceId, Category categoryName, Driver driverName) {
        this.id = new DriverPlaceGuessId(userId, raceId, categoryName);
        this.driverName = driverName;
    }

    public UUID userId() {
        return id.userId();
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public Category categoryName() {
        return id.categoryName();
    }

    public Driver driverName() {
        return driverName;
    }
}
