package no.voiestad.f1.league;

import no.voiestad.f1.league.domain.LeagueInvitationDTO;
import no.voiestad.f1.league.domain.LeagueRole;
import no.voiestad.f1.league.leagues.*;
import no.voiestad.f1.user.UserEntity;
import no.voiestad.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public List<UserEntity> getPotentialOwners(UUID leagueId, Year year) {
        return getMembers(leagueId).stream()
                .filter(userEntity -> isAllowedToOwnMoreLeagues(userEntity.id(), year))
                .toList();
    }

    public boolean isValidLeagueId(UUID leagueId) {
        return leagueRepository.existsById(leagueId);
    }

    public Optional<LeagueEntity> getLeagueFromId(UUID leagueId) {
        return leagueRepository.findById(leagueId);
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
        return leagueRoleRepository.countByLeagueRoleAndYear(userId, year, LeagueRole.OWNER) < 10;
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

    public Optional<Year> getYearIfChangeable(UUID leagueId) {
        return leagueRepository.getYearIfChangeable(leagueId);
    }

    public boolean inviteToLeague(UUID userId, UUID leagueId, UUID inviter) {
        LeagueInvitationId id = new LeagueInvitationId(userId, leagueId, inviter);
        if (leagueInvitationRepository.existsById(id)) {
            return false;
        }
        LeagueInvitationEntity entity = new LeagueInvitationEntity(userId, leagueId, inviter);
        leagueInvitationRepository.save(entity);
        return true;
    }

    public boolean uninviteToLeague(UUID userId, UUID leagueId, UUID inviter) {
        LeagueInvitationId id = new LeagueInvitationId(userId, leagueId, inviter);
        if (!leagueInvitationRepository.existsById(id)) {
            return false;
        }
        leagueInvitationRepository.deleteById(id);
        return true;
    }

    public void clearInvitationsByYear(Year year) {
        leagueInvitationRepository.clearInvitationsByYear(year);
    }

    public boolean isInvitedToLeague(UUID userId, UUID leagueId) {
        return leagueInvitationRepository.existsByIdUserIdAndIdLeagueId(userId, leagueId);
    }

    public void addUserToLeague(UUID userId, UUID leagueId) {
        LeagueMembershipEntity entity = new LeagueMembershipEntity(userId, leagueId);
        leagueMembershipRepository.save(entity);
        leagueInvitationRepository.deleteByIdUserIdAndIdLeagueId(userId, leagueId);
    }

    public boolean isMember(UUID userId, UUID leagueId) {
        return leagueMembershipRepository.existsByIdUserIdAndIdLeagueId(userId, leagueId);
    }

    public boolean hasRole(UUID userId, LeagueRole leagueRole, UUID leagueId) {
        return leagueRoleRepository.existsByIdUserIdAndIdLeagueRoleAndIdLeagueId(userId, leagueRole, leagueId);
    }

    public boolean canChangeLeague(UUID userId, UUID leagueId) {
        return hasRole(userId, LeagueRole.OWNER, leagueId);
    }

    public void deleteLeague(UUID leagueId) {
        leagueRepository.deleteById(leagueId);
    }

    public void transferOwnership(UUID userId, UUID leagueId) {
        leagueRoleRepository.deleteByIdLeagueIdAndIdLeagueRole(leagueId, LeagueRole.OWNER);
        leagueRoleRepository.save(new LeagueRoleEntity(userId, leagueId, LeagueRole.OWNER));
    }

    public void clearInvitationsByInviterAndLeague(UUID inviter, UUID leagueId) {
        leagueInvitationRepository.deleteByIdInviterAndIdLeagueId(inviter, leagueId);
    }

    public void clearSentInvitations(UUID inviter) {
        leagueInvitationRepository.deleteByIdInviter(inviter);
    }

    public void deleteUserFromLeague(UUID userId, UUID leagueId) {
        leagueMembershipRepository.deleteById(new LeagueMembershipId(userId, leagueId));
    }

    public boolean renameLeague(UUID leagueId, String leagueName) {
        Optional<LeagueEntity> optLeagueEntity = leagueRepository.findById(leagueId);
        if (optLeagueEntity.isEmpty()) {
            return false;
        }
        LeagueEntity leagueEntity = optLeagueEntity.get().withLeagueName(leagueName);
        leagueRepository.save(leagueEntity);
        return true;
    }

}
