package no.voiestad.f1.league;

import no.voiestad.f1.league.domain.LeagueInvitationDTO;
import no.voiestad.f1.league.leagues.*;
import no.voiestad.f1.user.UserEntity;
import no.voiestad.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LeagueService {
    private final LeagueRepository leagueRepository;
    private final LeagueMembershipRepository leagueMembershipRepository;
    private final LeagueRoleRepository leagueRoleRepository;
    private final LeagueInvitationRepository leagueInvitationRepository;

    public LeagueService(
            LeagueRepository leagueRepository,
            LeagueMembershipRepository leagueMembershipRepository,
            LeagueRoleRepository leagueRoleRepository,
            LeagueInvitationRepository leagueInvitationRepository
    ) {
        this.leagueRepository = leagueRepository;
        this.leagueMembershipRepository = leagueMembershipRepository;
        this.leagueRoleRepository = leagueRoleRepository;
        this.leagueInvitationRepository = leagueInvitationRepository;
    }

    public List<LeagueEntity> getLeagues(Year year) {
        return leagueRepository.findAllByYearOrderByLeagueName(year);
    }

    public List<LeagueEntity> getMemberships(UUID userId, Year year) {
        return leagueRepository.findAllByUserIdAndYearOrderByLeagueName(userId, year);
    }

    public List<UserEntity> getMembers(UUID leagueId) {
        return leagueMembershipRepository.findUsersByLeagueId(leagueId);
    }

    public boolean isValidLeagueId(UUID leagueId) {
        return leagueRepository.existsById(leagueId);
    }

    public List<LeagueInvitationDTO> getPendingInvitations(UUID userId) {
        return leagueInvitationRepository.findAllByUserId(userId).stream()
                .map(LeagueInvitationDTO::fromILeagueInvitation)
                .toList();
    }

    public List<LeagueInvitationDTO> getSentInvitations(UUID userId) {
        return leagueInvitationRepository.findAllByInviter(userId).stream()
                .map(LeagueInvitationDTO::fromILeagueInvitation)
                .toList();
    }
}
