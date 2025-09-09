package no.vebb.f1.guessing;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.year.Year;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CompetitorGuessId {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "position", nullable = false)
    private int position;

    @Embedded
    private Year year;

    protected CompetitorGuessId() {}

    public CompetitorGuessId(UUID userId, int position, Year year) {
        this.userId = userId;
        this.position = position;
        this.year = year;
    }

    public UUID userId() {
        return userId;
    }

    public int position() {
        return position;
    }

    public Year year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CompetitorGuessId that)) return false;
        return position == that.position && year == that.year && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, position, year);
    }
}
