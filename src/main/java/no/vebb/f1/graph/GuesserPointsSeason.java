package no.vebb.f1.graph;

import no.vebb.f1.util.domainPrimitive.Points;

import java.util.Collections;
import java.util.List;

public record GuesserPointsSeason(String name, List<Points> scores) {

	public GuesserPointsSeason(String name, List<Points> scores) {
		this.name = name;
		this.scores = Collections.unmodifiableList(scores);
	}
}
