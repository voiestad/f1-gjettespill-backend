package no.vebb.f1.guessing.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.util.exception.InvalidPositionException;
import org.springframework.lang.NonNull;

@Embeddable
public class GuessPosition implements Comparable<GuessPosition> {
    @Column(name = "position", nullable = false)
    private int value;

    protected GuessPosition() {}

    public GuessPosition(int value) {
        if (value < 1) {
            throw new InvalidPositionException("Positions can't be non-positive. Was " + value);
        }
        this.value = value;
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
    public int compareTo(GuessPosition o) {
        return Integer.compare(value, o.value);
    }
}
