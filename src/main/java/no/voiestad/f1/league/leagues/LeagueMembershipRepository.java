package no.voiestad.f1.league.leagues;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.user.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeagueMembershipRepository extends JpaRepository<LeagueMembershipEntity, LeagueMembershipId> {

    @Query("""
        SELECT u
        FROM LeagueMembershipEntity lm
        JOIN UserEntity u ON u.id = lm.id.userId
        WHERE lm.id.leagueId = :leagueId
        ORDER BY u.username
    """)
    List<UserEntity> findUsersByLeagueId(UUID leagueId);
}
