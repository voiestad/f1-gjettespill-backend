package no.vebb.f1.year;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Year implements Comparable<Year> {
	@Column(name = "year", nullable = false)
	public int value;

	protected Year() {}

	public Year(int value) {
		this.value = value;
	}

	@JsonValue
    public int toValue() {
        return value;
    }

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Year other = (Year) obj;
        return value == other.value;
    }

	@Override
	public int compareTo(Year o) {
		return Integer.compare(value, o.value);
	}
}
