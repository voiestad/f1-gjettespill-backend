package no.voiestad.f1.guessing.driverPlace;

import java.util.UUID;

import no.voiestad.f1.competitors.driver.DriverEntity;
import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.race.RaceId;

import jakarta.persistence.*;

@Entity
@Table(name = "driver_place_guesses")
public class DriverPlaceGuessEntity {
    @EmbeddedId
    private DriverPlaceGuessId id;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;

    protected DriverPlaceGuessEntity() {}

    public DriverPlaceGuessEntity(UUID userId, RaceId raceId, Category categoryName, DriverEntity driver) {
        this.id = new DriverPlaceGuessId(userId, raceId, categoryName);
        this.driver = driver;
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

    public DriverEntity driver() {
        return driver;
    }
}
