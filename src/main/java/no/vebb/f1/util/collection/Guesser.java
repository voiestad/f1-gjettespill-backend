package no.vebb.f1.util.collection;

import java.util.UUID;

import no.vebb.f1.util.domainPrimitive.Points;

public record Guesser(String username, Points points, UUID id) implements Comparable<Guesser> {

	@Override
	public int compareTo(Guesser other) {
		return points.compareTo(other.points);
	}
}
