package no.vebb.f1.collection;

import no.vebb.f1.results.collection.IColoredCompetitor;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;

public record ColoredCompetitor<T>(T competitor, Color color) {
    public static ColoredCompetitor<Driver> fromIColoredCompetitorToDriver(IColoredCompetitor iColoredCompetitor) {
        return new ColoredCompetitor<>(new Driver(iColoredCompetitor.getCompetitorName()), iColoredCompetitor.getColor());
    }
    public static ColoredCompetitor<Constructor> fromIColoredCompetitorToConstructor(IColoredCompetitor iColoredCompetitor) {
        return new ColoredCompetitor<>(new Constructor(iColoredCompetitor.getCompetitorName()), iColoredCompetitor.getColor());
    }
}
