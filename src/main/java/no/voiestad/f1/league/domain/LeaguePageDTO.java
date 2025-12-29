package no.voiestad.f1.league.domain;

import no.voiestad.f1.collection.RankedGuesser;
import no.voiestad.f1.placement.GuesserPointsSeason;
import no.voiestad.f1.user.PublicUserDto;

import java.util.List;

public record LeaguePageDTO(List<GuesserPointsSeason> graph,
                            List<RankedGuesser> leaderboard,
                            List<PublicUserDto> members,
                            LeagueMembershipStatus membershipStatus,
                            LeagueDTO league) {
}
