package no.vebb.f1.results;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StartingGridRepository extends JpaRepository<StartingGrid, StartingGridId> {
    @Query(value = "SELECT * FROM starting_grids WHERE race_id = :raceId ORDER BY position", nativeQuery = true)
    List<StartingGrid> findAllByRaceIdOrderByPosition(int raceId);

    @Query(value = "SELECT EXISTS (SELECT * from starting_grids WHERE race_id = :raceId)", nativeQuery = true)
    boolean existsByRaceId(int raceId);

    @Query(value = """
            SELECT DISTINCT *
            FROM starting_grids sg
            WHERE sg.race_id NOT IN (
            	SELECT rr.race_id
            	FROM race_results rr
            )
            """, nativeQuery = true)
    List<StartingGrid> findAllByNotInRaceResult();
}
