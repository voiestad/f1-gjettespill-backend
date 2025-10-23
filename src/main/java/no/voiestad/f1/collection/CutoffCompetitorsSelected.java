package no.voiestad.f1.collection;

import java.util.List;

import no.voiestad.f1.competitors.domain.CompetitorDTO;
import no.voiestad.f1.competitors.domain.CompetitorId;

public record CutoffCompetitorsSelected(
        List<CompetitorDTO> competitors,
        CompetitorId selected,
        long timeLeft,
        Race race) {
}
