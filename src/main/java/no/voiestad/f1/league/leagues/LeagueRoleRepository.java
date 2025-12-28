package no.voiestad.f1.league.leagues;

import java.util.UUID;

import no.voiestad.f1.league.domain.LeagueRole;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LeagueRoleRepository extends JpaRepository<LeagueRoleEntity, LeagueRoleId> {

    @Query("""
        SELECT COUNT(*)
        FROM LeagueRoleEntity lr
        JOIN LeagueEntity l ON l.leagueId = lr.id.leagueId
        WHERE lr.id.userId = :userId AND l.year = :year
        AND lr.id.leagueRole = :leagueRole
    """)
    int countByLeagueRoleAndYear(UUID userId, Year year, LeagueRole leagueRole);

    boolean existsByIdUserIdAndIdLeagueRoleAndIdLeagueId(UUID userId, LeagueRole leagueRole, UUID leagueId);

    @Modifying
    void deleteByIdLeagueIdAndIdLeagueRole(UUID leagueId, LeagueRole leagueRole);
}
