package no.vebb.f1.collection;

import no.vebb.f1.competitors.domain.CompetitorDTO;
import no.vebb.f1.competitors.domain.CompetitorId;

import java.util.List;

public record CutoffCompetitorsSelected(
        List<CompetitorDTO> competitors,
        CompetitorId selected,
        long timeLeft,
        Race race) {
}
