package no.voiestad.f1.collection;

import no.voiestad.f1.competitors.domain.CompetitorDTO;

import java.util.List;

public record CutoffCompetitors(List<CompetitorDTO> competitors, long timeLeft) {
}
