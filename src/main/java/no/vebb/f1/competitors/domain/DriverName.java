package no.vebb.f1.competitors.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DriverName implements CompetitorName {
	@Column(name = "driver_name", nullable = false)
	public String value;

	protected DriverName() {}

	public DriverName(String value) {
		this.value = value;
	}
	
	@JsonValue
    public String toValue() {
        return value;
    }

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		DriverName other = (DriverName) obj;
		if (value == null) {
            return other.value == null;
		} else return value.equals(other.value);
    }

	
}
