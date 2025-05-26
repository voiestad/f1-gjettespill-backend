package no.vebb.f1.util.collection;

import no.vebb.f1.util.exception.InvalidRankException;

public class RankedGuesser {

	public final int rank;
	public final Guesser guesser;	

	public RankedGuesser(Guesser guesser, int rank) {
		if (rank <= 0) {
			throw new InvalidRankException("Rank can't be non-positive");
		}
		this.rank = rank;
		this.guesser = guesser;
	}
}
