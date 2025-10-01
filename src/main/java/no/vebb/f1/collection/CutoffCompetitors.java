package no.vebb.f1.collection;

import no.vebb.f1.competitors.domain.CompetitorDTO;

import java.util.List;

public record CutoffCompetitors(List<CompetitorDTO> competitors, long timeLeft) {
}
