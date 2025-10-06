package no.vebb.f1.race;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RaceRepository extends JpaRepository<RaceEntity, RaceId> {
    @Query("""
            SELECT DISTINCT r
            FROM RaceEntity r
            JOIN RaceResultEntity rr ON r.raceId = rr.id.raceId
            WHERE r.year = :year
            ORDER BY r.position
            """)
    List<RaceEntity> findAllByYearJoinWithRaceResults(Year year);

    @Query("""
            SELECT DISTINCT r
            FROM RaceEntity r
            JOIN StartingGridEntity sg ON r.raceId = sg.id.raceId
            WHERE r.year = :year
            ORDER BY r.position
            """)
    List<RaceEntity> findAllByYearJoinWithStartingGrid(Year year);

    @Query("""
            SELECT DISTINCT r
            FROM RaceEntity r
            JOIN DriverStandingsEntity ds on ds.id.raceId = r.raceId
            JOIN ConstructorStandingsEntity cs on cs.id.raceId = r.raceId
            WHERE r.year = :year
            ORDER BY r.position
            """)
    List<RaceEntity> findAllByYearJoinWithStandings(Year year);

    @Query("""
            SELECT r
            FROM RaceEntity r
            WHERE r.raceId NOT IN (SELECT DISTINCT rr.id.raceId FROM RaceResultEntity rr)
            AND r.year = :year
            ORDER BY r.position
            """)
    List<RaceEntity> findAllByYearNotInRaceResult(Year year);

    @Query("""
            SELECT r
            FROM RaceEntity r
            WHERE r.raceId NOT IN (SELECT rr.id.raceId FROM RaceResultEntity rr)
            AND r.year NOT IN (SELECT yf.year FROM YearFinishedEntity yf)
            ORDER BY r.year, r.position
            """)
    List<RaceEntity> findAllByNotFinished();

    List<RaceEntity> findAllByYearOrderByPosition(Year year);

    Optional<RaceEntity> findTopByYearOrderByPositionDesc(Year year);

}
