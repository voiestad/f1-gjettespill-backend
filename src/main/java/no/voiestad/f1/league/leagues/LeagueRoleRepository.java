package no.voiestad.f1.league.leagues;

import java.util.UUID;

import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeagueRoleRepository extends JpaRepository<LeagueRoleEntity, LeagueRoleId> {

    @Query("""
        SELECT COUNT(*)
        FROM LeagueRoleEntity lr
        JOIN LeagueEntity l ON l.leagueId = lr.id.leagueId
        WHERE lr.id.userId = :userId AND l.year = :year
    """)
    int countByLeagueRoleAndYear(UUID userId, Year year);

}
