package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;
import no.vebb.f1.util.exception.InvalidPositionException;
import org.springframework.lang.NonNull;

public record Position(int value) {

    public Position {
        validate();
    }

    private void validate() throws InvalidPositionException {
        if (value < 1) {
            throw new InvalidPositionException("Positions can't be non-positive");
        }
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
}
