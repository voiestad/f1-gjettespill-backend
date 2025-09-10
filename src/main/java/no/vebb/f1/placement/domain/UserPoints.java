package no.vebb.f1.placement.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.util.exception.InvalidPointsException;

@Embeddable
public class UserPoints implements Comparable<UserPoints> {
	@Column(name = "points", nullable = false)
	public final int value;

	public UserPoints(int value) throws InvalidPointsException {
		this.value = value;
		validate();
	}

	public UserPoints() {
		this.value = 0;
	}

	private void validate() throws InvalidPointsException {
		if (value < 0) {
			throw new InvalidPointsException("Points can't be negative");
		}
	}

	public UserPoints add(UserPoints other) {
		return new UserPoints(this.value + other.value);
	}

	@JsonValue
    public int toValue() {
        return value;
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
		UserPoints other = (UserPoints) obj;
        return value == other.value;
    }

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(UserPoints o) {
		return Integer.compare(this.value, o.value);	
	}
}
