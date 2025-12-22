package no.voiestad.f1.league;

import no.voiestad.f1.league.domain.LeagueInvitationDTO;
import no.voiestad.f1.league.domain.LeagueRole;
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

    public boolean isAllowedToOwnMoreLeagues(UUID userId, Year year) {
        return leagueRoleRepository.countByLeagueRoleAndYear(userId, year) < 10;
    }

    public boolean isLeagueNameAvailable(String leagueName, Year year) {
        return !leagueRepository.existsByLeagueNameAndYear(leagueName, year);
    }

    public boolean hasValidLeagueNameFormat(String leagueName) {
        return leagueName.length() <= 50 && leagueName.matches("^[a-zA-ZÆØÅæøå0-9 ]+$");
    }

    public void addLeague(String leagueName, Year year, UUID userId) {
        LeagueEntity league = new LeagueEntity(UUID.randomUUID(), leagueName, year);
        leagueRepository.save(league);
        LeagueRoleEntity leagueRole = new LeagueRoleEntity(userId, league.leagueId(), LeagueRole.OWNER);
        leagueRoleRepository.save(leagueRole);
        LeagueMembershipEntity leagueMembership = new LeagueMembershipEntity(userId, league.leagueId());
        leagueMembershipRepository.save(leagueMembership);
    }
}
