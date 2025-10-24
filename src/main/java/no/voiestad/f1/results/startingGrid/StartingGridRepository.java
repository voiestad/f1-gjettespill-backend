package no.voiestad.f1.results.startingGrid;

import java.util.List;

import no.voiestad.f1.race.RaceId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StartingGridRepository extends JpaRepository<StartingGridEntity, StartingGridId> {
    List<StartingGridEntity> findAllByIdRaceIdOrderByPosition(RaceId raceId);

    boolean existsByIdRaceId(RaceId raceId);

    @Query(value = """
            SELECT *
            FROM starting_grids sg
            WHERE sg.race_id NOT IN (
            	SELECT rr.race_id
            	FROM race_results rr
            )
            """, nativeQuery = true)
    List<StartingGridEntity> findAllByNotInRaceResult();
}
