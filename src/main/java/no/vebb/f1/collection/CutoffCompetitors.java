package no.vebb.f1.collection;

import no.vebb.f1.competitors.domain.Competitor;

import java.util.List;

public record CutoffCompetitors<T extends Competitor>(List<T> competitors, long timeLeft) {
}
