package no.voiestad.f1.league.domain;

import no.voiestad.f1.league.leagues.LeagueEntity;
import no.voiestad.f1.user.UserEntity;

public interface ILeagueInviation {
    UserEntity getInvited();
    UserEntity getInviter();
    LeagueEntity getLeague();
}
