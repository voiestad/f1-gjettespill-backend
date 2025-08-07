package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Position;

public record Placement<T>(Position pos, T value) {
}
