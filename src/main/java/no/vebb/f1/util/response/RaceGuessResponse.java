package no.vebb.f1.util.response;

import java.util.List;

import no.vebb.f1.util.collection.UserRaceGuess;

public record RaceGuessResponse(String name, List<UserRaceGuess> first, List<UserRaceGuess> tenth) {
}
