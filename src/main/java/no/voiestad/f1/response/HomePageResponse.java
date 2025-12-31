package no.voiestad.f1.response;

import no.voiestad.f1.placement.GuesserPointsSeason;
import no.voiestad.f1.collection.RankedGuesser;
import no.voiestad.f1.user.PublicUserDto;

import java.time.LocalDateTime;
import java.util.List;

public record HomePageResponse(List<GuesserPointsSeason> graph,
                               List<RankedGuesser> leaderboard,
                               List<PublicUserDto> guessers,
                               LocalDateTime cutoff) {
}
