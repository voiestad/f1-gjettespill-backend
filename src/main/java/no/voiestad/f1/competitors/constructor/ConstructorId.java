package no.voiestad.f1.competitors.constructor;

import java.util.Objects;

import no.voiestad.f1.competitors.domain.CompetitorId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import com.fasterxml.jackson.annotation.JsonValue;

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
