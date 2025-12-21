package no.voiestad.f1.league.leagues;

import no.voiestad.f1.league.domain.ILeagueInviation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LeagueInvitationRepository extends JpaRepository<LeagueInvitationEntity, LeagueInvitationId> {

    @Query("""
        SELECT u1 as invited, u2 as inviter, l as league
        FROM LeagueEntity l
        JOIN LeagueInvitationEntity li ON l.leagueId = li.id.leagueId
        JOIN UserEntity u1 ON u1.id = li.id.userId
        JOIN UserEntity u2 ON u2.id = li.id.inviter
        WHERE u1.id = :userId
        ORDER BY l.leagueName
    """)
    List<ILeagueInviation> findAllByUserId(UUID userId);

    @Query("""
        SELECT u1 as invited, u2 as inviter, l as league
        FROM LeagueEntity l
        JOIN LeagueInvitationEntity li ON l.leagueId = li.id.leagueId
        JOIN UserEntity u1 ON u1.id = li.id.userId
        JOIN UserEntity u2 ON u2.id = li.id.inviter
        WHERE u2.id = :inviter
        ORDER BY l.leagueName
    """)
    List<ILeagueInviation> findAllByInviter(UUID inviter);
}
