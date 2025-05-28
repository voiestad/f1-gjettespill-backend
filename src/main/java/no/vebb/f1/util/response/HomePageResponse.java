package no.vebb.f1.util.response;

import no.vebb.f1.graph.GuesserPointsSeason;
import no.vebb.f1.util.collection.RankedGuesser;

import java.util.List;

public record HomePageResponse(List<GuesserPointsSeason> graph,
                               List<RankedGuesser> leaderboard,
                               List<String> guessers) {
}
