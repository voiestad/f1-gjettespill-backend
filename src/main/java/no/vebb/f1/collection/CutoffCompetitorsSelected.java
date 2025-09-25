package no.vebb.f1.collection;

import no.vebb.f1.competitors.domain.Competitor;

import java.util.List;

public record CutoffCompetitorsSelected<T extends Competitor>(
        List<T> competitors,
        T selected,
        long timeLeft,
        Race race) {
}
