package no.voiestad.f1.results.request;

import java.util.List;

public record RaceResultRequestBody(
        Integer raceId,
        List<RaceResultRequest> raceResult,
        List<DriverStandingsRequest> driverStandings,
        List<ConstructorStandingsRequest> constructorStandings) {
}
