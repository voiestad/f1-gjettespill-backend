package no.voiestad.f1.results.request;

public record ConstructorStandingsRequest(
        Integer constructor,
        Integer position,
        Integer points) {
}
