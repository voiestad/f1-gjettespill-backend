package no.vebb.f1.response;

import no.vebb.f1.placement.GuesserPointsSeason;
import no.vebb.f1.collection.RankedGuesser;

import java.util.List;

public record HomePageResponse(List<GuesserPointsSeason> graph,
                               List<RankedGuesser> leaderboard,
                               List<String> guessers) {
}
