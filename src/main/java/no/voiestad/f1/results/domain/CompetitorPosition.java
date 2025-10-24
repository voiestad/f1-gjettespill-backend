package no.voiestad.f1.results.domain;

import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.lang.NonNull;
import com.fasterxml.jackson.annotation.JsonValue;

@Embeddable
public class CompetitorPosition implements Comparable<CompetitorPosition> {
    @Column(name = "position", nullable = false)
    private int value;

    public CompetitorPosition() {
        this.value = 1;
    }

    private CompetitorPosition(int value) {
        this.value = value;
    }

    public static Optional<CompetitorPosition> getCompetitorPosition(int value) {
        if (value < 1) {
            return Optional.empty();
        }
        return Optional.of(new CompetitorPosition(value));
    }

    public CompetitorPosition next() {
        return new CompetitorPosition(this.value + 1);
    }

    @JsonValue
    public int toValue() {
        return value;
    }

    @Override
    @NonNull
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CompetitorPosition that)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public int compareTo(CompetitorPosition o) {
        return Integer.compare(value, o.value);
    }
}
