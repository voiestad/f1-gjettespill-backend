package no.vebb.f1.graph;

import java.util.Collections;
import java.util.List;

public record GuesserPointsSeason(String name, List<Integer> scores) {

	public GuesserPointsSeason(String name, List<Integer> scores) {
		this.name = name;
		this.scores = Collections.unmodifiableList(scores);
	}
}
