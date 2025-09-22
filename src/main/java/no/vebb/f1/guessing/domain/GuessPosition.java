package no.vebb.f1.guessing.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Embeddable
public class GuessPosition implements Comparable<GuessPosition> {
    @Column(name = "position", nullable = false)
    private int value;

    public GuessPosition() {
        this.value = 1;
    }

    private GuessPosition(int value) {
        this.value = value;
    }

    public GuessPosition next() {
        return new GuessPosition(this.value + 1);
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
        if (!(o instanceof GuessPosition that)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public int compareTo(GuessPosition o) {
        return Integer.compare(value, o.value);
    }
}
