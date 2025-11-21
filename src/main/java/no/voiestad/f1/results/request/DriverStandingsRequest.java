package no.voiestad.f1.results.request;

public record DriverStandingsRequest(
        Integer driver,
        Integer position,
        Integer points) {
}
