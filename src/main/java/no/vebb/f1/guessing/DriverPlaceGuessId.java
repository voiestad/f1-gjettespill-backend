package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DriverPlaceGuessId {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "race_id")
    private int raceId;

    @Column(name = "category_name")
    private String categoryName;

    protected DriverPlaceGuessId() {}

    public DriverPlaceGuessId(UUID userId, int raceId, String categoryName) {
        this.userId = userId;
        this.raceId = raceId;
        this.categoryName = categoryName;
    }

    public UUID userId() {
        return userId;
    }

    public int raceId() {
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
