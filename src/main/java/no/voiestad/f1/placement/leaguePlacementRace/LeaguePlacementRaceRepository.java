package no.voiestad.f1.placement.leaguePlacementRace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.placement.collection.PlacementGraphResult;
import no.voiestad.f1.placement.collection.PlacementLeaderboardResult;
import no.voiestad.f1.placement.collection.PositionResult;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LeaguePlacementRaceRepository extends JpaRepository<LeaguePlacementRaceEntity, LeaguePlacementRaceId> {
    Optional<LeaguePlacementRaceEntity> findAllByIdRaceIdAndIdUserIdAndIdLeagueId(RaceId raceId, UUID userId, UUID leagueId);
    @Query(value = """
                SELECT r.position as position
                FROM LeaguePlacementRaceEntity pr
                JOIN RaceEntity r ON r.raceId = pr.id.raceId
                WHERE r.year = :year AND pr.id.leagueId = :leagueId
                GROUP BY r.position
                ORDER BY position
            """)
    List<PositionResult> findAllByYearOrderByPosition(Year year, UUID leagueId);
    @Query(value = """
                SELECT user_id, username, points, position, placement
                FROM (SELECT prys.user_id as user_id, u.username :: text as username, prys2.points as points, 0::INTEGER as position, prys.placement as placement
                FROM league_placements_race_year_start prys
                JOIN users u ON u.user_id = prys.user_id
                JOIN placements_race_year_start prys2 ON prys2.user_id = prys.user_id
                WHERE prys.year = :year AND prys.league_id = :leagueId
                UNION
                (SELECT pr.user_id AS user_id, u.username :: text AS username, pr2.points AS points, r.position AS position, pr.placement as placement
                FROM league_placements_race pr
                JOIN races r ON r.race_id = pr.race_id
                JOIN users u ON u.user_id = pr.user_id
                JOIN placements_race pr2 ON pr2.race_id = pr.race_id AND pr2.user_id = pr.user_id
                WHERE r.year = :year AND pr.league_id = :leagueId)) as placements_race
                WHERE position = :position
                ORDER BY placement, username;
            """, nativeQuery = true)
    List<PlacementLeaderboardResult> getPlacementsByPositionAndYear(int position, int year, UUID leagueId);
    @Query(value = """
                    SELECT prys.user_id as user_id, u.username :: text as username, prys2.points as points, 0::INTEGER as position
                    FROM league_placements_race_year_start prys
                    JOIN users u ON u.user_id = prys.user_id
                    JOIN placements_race_year_start prys2 ON prys2.user_id = prys.user_id
                    WHERE prys.year = :year AND prys.league_id = :leagueId
                    UNION
                    (SELECT pr.user_id AS user_id, u.username :: text as username, pr2.points AS points, r.position AS position
                    FROM league_placements_race pr
                    JOIN races r ON r.race_id = pr.race_id
                    JOIN users u ON u.user_id = pr.user_id
                    JOIN placements_race pr2 ON pr2.race_id = pr.race_id AND pr2.user_id = pr.user_id
                    WHERE r.year = :year AND pr.league_id = :leagueId)
                    ORDER BY position, username;
                """, nativeQuery = true)
    List<PlacementGraphResult> findAllByYear(int year, UUID leagueId);

    @Modifying
    void deleteByIdUserIdAndIdLeagueId(UUID userId, UUID leagueId);

    @Modifying
    void deleteByIdLeagueId(UUID leagueId);
}
