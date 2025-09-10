package no.vebb.f1.guessing.driverPlace;

import jakarta.persistence.*;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.race.RaceId;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DriverPlaceGuessId {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Embedded
    private RaceId raceId;

    @Column(name = "category_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category categoryName;

    protected DriverPlaceGuessId() {}

    public DriverPlaceGuessId(UUID userId, RaceId raceId, Category categoryName) {
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

    public Category categoryName() {
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
