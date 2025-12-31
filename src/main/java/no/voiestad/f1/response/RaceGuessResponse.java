package no.voiestad.f1.response;

import java.util.List;

import no.voiestad.f1.guessing.collection.UserQualifyingGuess;
import no.voiestad.f1.guessing.collection.UserRaceGuess;

public record RaceGuessResponse(String name, List<UserRaceGuess> first, List<UserRaceGuess> tenth,
                                List<UserQualifyingGuess> pole) {
}
