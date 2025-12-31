package no.voiestad.f1.league.domain;

import no.voiestad.f1.user.PublicUserDto;

public record LeagueInvitationDTO(PublicUserDto invited, PublicUserDto inviter, LeagueDTO league) {
    public static LeagueInvitationDTO fromILeagueInvitation(ILeagueInviation leagueInviation) {
        return new LeagueInvitationDTO(
                PublicUserDto.fromEntity(leagueInviation.getInvited()),
                PublicUserDto.fromEntity(leagueInviation.getInviter()),
                LeagueDTO.fromEntity(leagueInviation.getLeague())
        );
    }
}
