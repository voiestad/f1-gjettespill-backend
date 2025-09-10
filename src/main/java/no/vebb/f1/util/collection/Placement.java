package no.vebb.f1.util.collection;

import no.vebb.f1.guessing.GuessPosition;

public record Placement<T>(GuessPosition pos, T value) {
}
