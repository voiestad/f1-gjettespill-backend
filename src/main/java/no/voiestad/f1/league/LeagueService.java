package no.voiestad.f1.league;

import no.voiestad.f1.league.leagues.LeagueInvitationRepository;
import no.voiestad.f1.league.leagues.LeagueMembershipRepository;
import no.voiestad.f1.league.leagues.LeagueRepository;
import no.voiestad.f1.league.leagues.LeagueRoleRepository;
import org.springframework.stereotype.Service;

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
}
