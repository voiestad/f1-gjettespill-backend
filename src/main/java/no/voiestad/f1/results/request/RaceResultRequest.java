package no.voiestad.f1.results.request;

public record RaceResultRequest(
        String position,
        Integer driver,
        Integer points,
        Integer finishingPosition) {
}
