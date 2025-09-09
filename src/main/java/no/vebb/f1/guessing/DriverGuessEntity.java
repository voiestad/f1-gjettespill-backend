package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.Year;

import java.util.UUID;

@Entity
@Table(name = "driver_guesses")
public class DriverGuessEntity {
    @EmbeddedId
    private CompetitorGuessId id;

    @Column(name = "driver_name")
    private String driverName;

    protected DriverGuessEntity() {}

    public DriverGuessEntity(UUID userId, int position, Year year, String driverName) {
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

    public String driverName() {
        return driverName;
    }
}
