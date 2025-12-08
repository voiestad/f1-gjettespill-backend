package no.voiestad.f1.league.leagues;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueInvitationRepository extends JpaRepository<LeagueInvitationEntity, LeagueInvitationId> {
}
