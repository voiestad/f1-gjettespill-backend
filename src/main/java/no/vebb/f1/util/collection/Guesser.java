package no.vebb.f1.util.collection;

import java.util.UUID;

public class Guesser implements Comparable<Guesser> {

	public final String username;
	public final int points;
	public final UUID id;

	public Guesser(String username, int points, UUID id) {
		this.username = username;
		this.points = points;
		this.id = id;
	}

	@Override
	public int compareTo(Guesser other) {
		if (points < other.points) {
			return 1;
		} else if (points > other.points) {
			return -1;
		}
		return 0;
	}
}
