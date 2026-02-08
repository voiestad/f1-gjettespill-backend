package no.voiestad.f1.results.request;

import java.util.List;

public record RaceResultRequestBody(
        Integer raceId,
        List<Integer> raceResult,
        List<Integer> driverStandings,
        List<Integer> constructorStandings) {
}
