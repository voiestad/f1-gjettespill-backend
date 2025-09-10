package no.vebb.f1.response;

import java.util.List;

import no.vebb.f1.guessing.collection.UserRaceGuess;

public record RaceGuessResponse(String name, List<UserRaceGuess> first, List<UserRaceGuess> tenth) {
}
