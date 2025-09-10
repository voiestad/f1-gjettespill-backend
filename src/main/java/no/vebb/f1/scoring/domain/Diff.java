package no.vebb.f1.scoring.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Diff {
	@Column(name = "diff", nullable = false)
	private int value;

	public Diff() {
		this.value = 0;
	}

	public Diff(int value) {
		this.value = Math.abs(value);
	}

	@JsonValue
    public int toValue() {
        return value;
    }

	public Diff add(Diff other) {
		return new Diff(this.value + other.value);
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
		Diff other = (Diff) obj;
        return value == other.value;
    }
	
}
