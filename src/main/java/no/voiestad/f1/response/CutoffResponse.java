package no.voiestad.f1.response;

import java.time.LocalDateTime;
import java.util.List;

import no.voiestad.f1.collection.CutoffRace;

public record CutoffResponse(List<CutoffRace> cutoffRaces, LocalDateTime cutoffYear) {
}
