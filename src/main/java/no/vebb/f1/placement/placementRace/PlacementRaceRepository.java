package no.vebb.f1.placement.placementRace;

import no.vebb.f1.placement.collection.PlacementGraphResult;
import no.vebb.f1.placement.collection.PlacementLeaderboardResult;
import no.vebb.f1.placement.collection.PositionResult;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlacementRaceRepository extends JpaRepository<PlacementRaceEntity, PlacementRaceId> {
    Optional<PlacementRaceEntity> findAllByIdRaceIdAndIdUserId(RaceId raceId, UUID userId);
    @Query(value = """
                SELECT r.position as position
                FROM PlacementRaceEntity pr
                JOIN RaceEntity r ON r.raceId = pr.id.raceId
                WHERE r.year = :year
                GROUP BY r.position
                ORDER BY position
            """)
    List<PositionResult> findAllByYearOrderByPosition(Year year);
    @Query(value = """
                SELECT user_id, username, points, position, placement
                FROM (SELECT prys.user_id as user_id, u.username :: text as username, prys.points as points, 0::INTEGER as position, prys.placement as placement
                FROM placements_race_year_start prys
                JOIN users u ON u.user_id = prys.user_id
                WHERE year = :year
                UNION
                (SELECT pr.user_id AS user_id, u.username :: text as username, pr.points AS points, r.position AS position, pr.placement as placement
                FROM placements_race pr
                JOIN races r ON r.race_id = pr.race_id
                JOIN users u ON u.user_id = pr.user_id
                WHERE year = :year)) as placements_race
                WHERE position = :position
                ORDER BY placement, username;
            """, nativeQuery = true)
    List<PlacementLeaderboardResult> getPlacementsByPositionAndYear(int position, int year);
    @Query(value = """
                    SELECT prys.user_id as user_id, u.username :: text as username, prys.points as points, 0::INTEGER as position
                    FROM placements_race_year_start prys
                    JOIN users u ON u.user_id = prys.user_id
                    WHERE year = :year
                    UNION
                    (SELECT pr.user_id AS user_id, u.username :: text as username, pr.points AS points, r.position AS position
                    FROM placements_race pr
                    JOIN races r ON r.race_id = pr.race_id
                    JOIN users u ON u.user_id = pr.user_id
                    WHERE year = :year)
                    ORDER BY position, username;
                """, nativeQuery = true)
    List<PlacementGraphResult> findAllByYear(int year);
}
