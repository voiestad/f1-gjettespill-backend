package no.vebb.f1.util.collection;

import no.vebb.f1.guessing.GuessPosition;

public record RankedGuesser(Guesser guesser, GuessPosition rank) {

    public boolean hasPoints() {
        return guesser.points().value > 0;
    }
}
