package no.vebb.f1.competitors.constructor;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.competitors.domain.CompetitorId;

import java.util.Objects;

@Embeddable
public class ConstructorId implements CompetitorId {
    @Column(name = "constructor_id", nullable = false)
    private int value;

    protected ConstructorId() {}

    public ConstructorId(int value) {
        this.value = value;
    }

    @JsonValue
    public int toValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorId constructorId)) return false;
        return value == constructorId.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
