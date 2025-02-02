package no.vebb.f1.util.collection;

import java.util.UUID;

import no.vebb.f1.util.domainPrimitive.Points;

public class Guesser implements Comparable<Guesser> {

	public final String username;
	public final Points points;
	public final UUID id;

	public Guesser(String username, Points points, UUID id) {
		this.username = username;
		this.points = points;
		this.id = id;
	}

	@Override
	public int compareTo(Guesser other) {
		return points.compareTo(points);
	}
}
