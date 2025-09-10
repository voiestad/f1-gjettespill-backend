package no.vebb.f1.response;

import no.vebb.f1.collection.CutoffRace;

import java.time.LocalDateTime;
import java.util.List;

public record CutoffResponse(List<CutoffRace> cutoffRaces, LocalDateTime cutoffYear) {
}
