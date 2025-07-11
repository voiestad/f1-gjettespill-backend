package no.vebb.f1.util.collection;

import no.vebb.f1.util.exception.InvalidRankException;

public record RankedGuesser(Guesser guesser, int rank) {

	/**
	 * @throws InvalidRankException if rank is non-positive
	 */
	public RankedGuesser {
		if (rank <= 0) {
			throw new InvalidRankException("Rank can't be non-positive");
		}
	}
}
