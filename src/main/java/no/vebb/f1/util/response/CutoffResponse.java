package no.vebb.f1.util.response;

import no.vebb.f1.util.collection.CutoffRace;

import java.time.LocalDateTime;
import java.util.List;

public record CutoffResponse(List<CutoffRace> cutoffRaces, LocalDateTime cutoffYear) {
}
