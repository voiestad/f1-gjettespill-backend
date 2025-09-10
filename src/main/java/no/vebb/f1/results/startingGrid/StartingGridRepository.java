package no.vebb.f1.results.startingGrid;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.collection.IColoredCompetitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StartingGridRepository extends JpaRepository<StartingGridEntity, StartingGridId> {
    List<StartingGridEntity> findAllByIdRaceIdOrderByPosition(RaceId raceId);

    boolean existsByIdRaceId(RaceId raceId);

    @Query(value = """
            SELECT DISTINCT *
            FROM starting_grids sg
            WHERE sg.race_id NOT IN (
            	SELECT rr.race_id
            	FROM race_results rr
            )
            """, nativeQuery = true)
    List<StartingGridEntity> findAllByNotInRaceResult();

    @Query("""
            SELECT sg.id.driverName as competitorName, cc.color as color
            FROM StartingGridEntity sg
            LEFT JOIN DriverTeamEntity dt ON dt.id.driverName = sg.id.driverName
            LEFT JOIN ConstructorColorEntity cc ON cc.id.constructorName = dt.team
            WHERE sg.id.raceId = :raceId
            ORDER BY sg.position
            """)
    List<IColoredCompetitor> findAllByRaceIdWithColor(RaceId raceId);
}
