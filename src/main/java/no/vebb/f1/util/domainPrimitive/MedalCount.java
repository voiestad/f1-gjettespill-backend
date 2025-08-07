package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;
import no.vebb.f1.util.exception.InvalidMedalCountException;
import org.springframework.lang.NonNull;

public record MedalCount(int value) {

    public MedalCount {
        validate();
    }

    private void validate() throws InvalidMedalCountException {
        if (value < 0) {
            throw new InvalidMedalCountException("MedalCount can't be negative");
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
