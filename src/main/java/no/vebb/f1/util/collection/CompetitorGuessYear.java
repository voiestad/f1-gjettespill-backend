package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Year;

public record CompetitorGuessYear<T>(int position, T competitor, Year year) {
}
