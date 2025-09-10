package no.vebb.f1.util.collection;

import no.vebb.f1.placement.domain.UserPosition;

public record RankedGuesser(Guesser guesser, UserPosition rank) {

    public boolean hasPoints() {
        return guesser.points().value > 0;
    }
}
