package no.vebb.f1.util.collection;

import no.vebb.f1.results.IColoredCompetitor;
import no.vebb.f1.util.domainPrimitive.Color;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;

public record ColoredCompetitor<T>(T competitor, Color color) {
    public static ColoredCompetitor<Driver> fromIColoredCompetitorToDriver(IColoredCompetitor iColoredCompetitor) {
        return new ColoredCompetitor<>(new Driver(iColoredCompetitor.getCompetitorName()), new Color(iColoredCompetitor.getColor()));
    }
    public static ColoredCompetitor<Constructor> fromIColoredCompetitorToConstructor(IColoredCompetitor iColoredCompetitor) {
        return new ColoredCompetitor<>(new Constructor(iColoredCompetitor.getCompetitorName()), new Color(iColoredCompetitor.getColor()));
    }
}
