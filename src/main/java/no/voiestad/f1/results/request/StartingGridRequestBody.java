package no.voiestad.f1.results.request;

import java.util.List;

public record StartingGridRequestBody(
        Integer raceId,
        List<Integer> startingGrid) {
}
