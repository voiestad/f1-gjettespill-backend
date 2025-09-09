package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.util.domainPrimitive.RaceId;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DriverPlaceGuessId {
    @Column(name = "user_id")
    private UUID userId;

    @Embedded
    private RaceId raceId;

    @Column(name = "category_name")
    private String categoryName;

    protected DriverPlaceGuessId() {}

    public DriverPlaceGuessId(UUID userId, RaceId raceId, String categoryName) {
        this.userId = userId;
        this.raceId = raceId;
        this.categoryName = categoryName;
    }

    public UUID userId() {
        return userId;
    }

    public RaceId raceId() {
        return raceId;
    }

    public String categoryName() {
        return categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DriverPlaceGuessId that)) return false;
        return raceId == that.raceId && Objects.equals(userId, that.userId) && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, raceId, categoryName);
    }
}
