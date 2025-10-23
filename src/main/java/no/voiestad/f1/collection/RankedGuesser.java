package no.voiestad.f1.collection;

import no.voiestad.f1.placement.collection.Guesser;
import no.voiestad.f1.placement.domain.UserPosition;

public record RankedGuesser(Guesser guesser, UserPosition rank) {

    public boolean hasPoints() {
        return guesser.points().value > 0;
    }
}
