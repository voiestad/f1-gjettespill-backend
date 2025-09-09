package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.RaceId;

import java.util.UUID;

@Entity
@Table(name = "driver_place_guesses")
public class DriverPlaceGuessEntity {
    @EmbeddedId
    private DriverPlaceGuessId id;

    @Column(name = "driver_name")
    private String driverName;

    protected DriverPlaceGuessEntity() {}

    public DriverPlaceGuessEntity(UUID userId, RaceId raceId, String categoryName, String driverName) {
        this.id = new DriverPlaceGuessId(userId, raceId, categoryName);
        this.driverName = driverName;
    }

    public UUID userId() {
        return id.userId();
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public String categoryName() {
        return id.categoryName();
    }

    public String driverName() {
        return driverName;
    }
}
