package no.vebb.f1.placement;

import no.vebb.f1.placement.domain.UserPoints;

import java.util.Collections;
import java.util.List;

public record GuesserPointsSeason(String name, List<UserPoints> scores) {

	public GuesserPointsSeason(String name, List<UserPoints> scores) {
		this.name = name;
		this.scores = Collections.unmodifiableList(scores);
	}

	public boolean hasPoints() {
		if (scores.isEmpty()) {
			return false;
		}
		return scores.get(scores.size() - 1).value > 0;
	}
}
