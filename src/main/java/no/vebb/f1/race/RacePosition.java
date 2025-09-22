package no.vebb.f1.race;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.Optional;

@Embeddable
public class RacePosition implements Comparable<RacePosition> {
    @Column(name = "position", nullable = false)
    private int value;

    public RacePosition() {
        this.value = 1;
    }

    private RacePosition(int value) {
        this.value = value;
    }

    public static Optional<RacePosition> getRacePosition(int value) {
        if (value < 1) {
            return Optional.empty();
        }
        return Optional.of(new RacePosition(value));
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
    public boolean equals(Object o) {
        if (!(o instanceof RacePosition that)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public int compareTo(RacePosition o) {
        return Integer.compare(value, o.value);
    }
}
