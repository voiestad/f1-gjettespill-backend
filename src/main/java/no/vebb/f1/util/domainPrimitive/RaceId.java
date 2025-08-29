package no.vebb.f1.util.domainPrimitive;

import com.fasterxml.jackson.annotation.JsonValue;

import no.vebb.f1.race.RaceService;
import no.vebb.f1.util.exception.InvalidRaceException;

public class RaceId {
	public final int value;
	private RaceService raceService;

	public RaceId(int value, RaceService raceService) throws InvalidRaceException {
		this.value = value;
		this.raceService = raceService;
		validate();
	}

	public RaceId(int value) {
		this.value = value;
	}

	private void validate() throws InvalidRaceException {
		if (!raceService.isRaceAdded(value)) {
			throw new InvalidRaceException("RaceId : " + value + " is not a race ID");
		}
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
		RaceId other = (RaceId) obj;
        return value == other.value;
    }
}
