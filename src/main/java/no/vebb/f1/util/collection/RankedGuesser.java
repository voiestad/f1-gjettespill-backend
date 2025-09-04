package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Position;

public record RankedGuesser(Guesser guesser, Position rank) {
    public boolean hasPoints() {
        return guesser.points().value > 0;
    }
}
