package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RaceId implements Comparable<RaceId> {
    @Column(name = "race_id", nullable = false)
    private int value;

    public RaceId(int value) {
        this.value = value;
    }

    protected RaceId() {
    }

    @JsonValue
    public int toValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceId other = (RaceId) obj;
        return value == other.value;
    }

    public int compareTo(RaceId o) {
        return Integer.compare(value, o.value);
    }
}
