package no.vebb.f1.results.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.util.exception.InvalidPositionException;
import org.springframework.lang.NonNull;

@Embeddable
public class CompetitorPosition implements Comparable<CompetitorPosition> {
    @Column(name = "position", nullable = false)
    private int value;

    public CompetitorPosition() {
        this.value = 1;
    }

    public CompetitorPosition(int value) {
        if (value < 1) {
            throw new InvalidPositionException("Positions can't be non-positive. Was " + value);
        }
        this.value = value;
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
    public int compareTo(CompetitorPosition o) {
        return Integer.compare(value, o.value);
    }
}
