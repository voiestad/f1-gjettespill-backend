package no.vebb.f1.placement.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Embeddable
public class UserPosition implements Comparable<UserPosition> {
    @Column(name = "placement", nullable = false)
    private int value;

    public UserPosition() {
        this.value = 1;
    }

    private UserPosition(int value) {
        this.value = value;
    }

    public static Optional<UserPosition> getUserPosition(int value) {
        if (value < 1) {
            return Optional.empty();
        }
        return Optional.of(new UserPosition(value));
    }

    public UserPosition next() {
        return new UserPosition(this.value + 1);
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
