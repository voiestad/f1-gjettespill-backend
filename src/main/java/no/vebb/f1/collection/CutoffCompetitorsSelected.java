package no.vebb.f1.collection;

import java.util.List;

public record CutoffCompetitorsSelected<T>(
        List<ColoredCompetitor<T>> competitors,
        T selected,
        long timeLeft,
        Race race) {
}
