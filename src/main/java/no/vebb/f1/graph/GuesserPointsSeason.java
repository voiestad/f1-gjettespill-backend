package no.vebb.f1.graph;

import java.util.Collections;
import java.util.List;

public class GuesserPointsSeason {
	
	public final String name;
	public final List<Integer> scores;

	public GuesserPointsSeason(String name, List<Integer> scores) {
		this.name = name;
		this.scores = Collections.unmodifiableList(scores);
	}
}
