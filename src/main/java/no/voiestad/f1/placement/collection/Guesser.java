package no.voiestad.f1.placement.collection;

import java.util.UUID;

import no.voiestad.f1.placement.domain.UserPoints;

public record Guesser(String username, UserPoints points, UUID id) implements Comparable<Guesser> {

	@Override
	public int compareTo(Guesser other) {
		return points.compareTo(other.points);
	}
}
