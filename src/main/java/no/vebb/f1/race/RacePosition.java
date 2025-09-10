package no.vebb.f1.race;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.exception.InvalidPositionException;
import org.springframework.lang.NonNull;

@Embeddable
public class RacePosition implements Comparable<RacePosition> {
    @Column(name = "position", nullable = false)
    private int value;

    public RacePosition() {
        this.value = 1;
    }

    public RacePosition(int value) {
        if (value < 1) {
            throw new InvalidPositionException("Positions can't be non-positive. Was " + value);
        }
        this.value = value;
    }

    public RacePosition next() {
        return new RacePosition(this.value + 1);
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
    public int compareTo(RacePosition o) {
        return Integer.compare(value, o.value);
    }
}
