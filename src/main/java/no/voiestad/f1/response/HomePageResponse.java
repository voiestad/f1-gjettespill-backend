package no.voiestad.f1.response;

import no.voiestad.f1.placement.GuesserPointsSeason;
import no.voiestad.f1.collection.RankedGuesser;

import java.util.List;

public record HomePageResponse(List<GuesserPointsSeason> graph,
                               List<RankedGuesser> leaderboard,
                               List<String> guessers) {
}
