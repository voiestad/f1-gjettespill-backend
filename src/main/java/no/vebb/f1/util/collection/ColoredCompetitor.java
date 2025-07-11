package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Color;

public record ColoredCompetitor<T>(T competitor, Color color) {
}
