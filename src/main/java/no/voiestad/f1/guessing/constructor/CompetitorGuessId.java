package no.voiestad.f1.guessing.constructor;

import java.util.Objects;
import java.util.UUID;

import no.voiestad.f1.guessing.domain.GuessPosition;
import no.voiestad.f1.year.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class CompetitorGuessId {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Embedded
    private GuessPosition position;

    @Embedded
    private Year year;

    protected CompetitorGuessId() {}

    public CompetitorGuessId(UUID userId, GuessPosition position, Year year) {
        this.userId = userId;
        this.position = position;
        this.year = year;
    }

    public UUID userId() {
        return userId;
    }

    public GuessPosition position() {
        return position;
    }

    public Year year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CompetitorGuessId that)) return false;
        return Objects.equals(position, that.position) && Objects.equals(year, that.year) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, position, year);
    }
}
