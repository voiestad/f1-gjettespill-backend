package no.vebb.f1.util.collection;

import java.util.List;

public record CutoffCompetitors<T>(List<ColoredCompetitor<T>> competitors, long timeLeft) {
}
