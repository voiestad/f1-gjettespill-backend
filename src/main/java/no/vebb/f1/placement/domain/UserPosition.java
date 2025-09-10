package no.vebb.f1.placement.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.util.exception.InvalidPositionException;
import org.springframework.lang.NonNull;

@Embeddable
public class UserPosition implements Comparable<UserPosition> {
    @Column(name = "placement", nullable = false)
    private int value;

    protected UserPosition() {}

    public UserPosition(int value) {
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
    public int compareTo(UserPosition o) {
        return Integer.compare(value, o.value);
    }
}
