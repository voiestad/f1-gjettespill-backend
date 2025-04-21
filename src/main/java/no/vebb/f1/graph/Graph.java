package no.vebb.f1.graph;

import java.util.Collections;
import java.util.List;

public class Graph {
	
	List<String> guessers;
	List<List<Integer>> scores;

	public List<String> getGuessers() {
		return Collections.unmodifiableList(guessers);
	}

	public List<List<Integer>> getScores() {
		return Collections.unmodifiableList(scores);
	}

}
