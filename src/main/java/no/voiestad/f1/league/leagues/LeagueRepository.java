package no.voiestad.f1.league.leagues;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeagueRepository extends JpaRepository<LeagueEntity, UUID> {

    List<LeagueEntity> findAllByYearOrderByLeagueName(Year year);

    @Query("""
        SELECT l
        FROM LeagueEntity l
        JOIN LeagueMembershipEntity lm ON l.leagueId = lm.id.leagueId
        WHERE lm.id.userId = :userId AND l.year = :year
        ORDER BY l.leagueName
    """)
    List<LeagueEntity> findAllByUserIdAndYearOrderByLeagueName(UUID userId, Year year);

    boolean existsByLeagueNameAndYear(String leagueName, Year year);

    @Query("""
        SELECT l.year as year
        FROM LeagueEntity l
        WHERE l.leagueId = :leagueId
        AND l.year NOT IN (SELECT yf.year from YearFinishedEntity yf)
        ORDER BY l.year
        LIMIT 1
    """)
    Optional<Year> getYearIfChangeable(UUID leagueId);
}
