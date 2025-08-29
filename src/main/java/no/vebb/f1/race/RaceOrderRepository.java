package no.vebb.f1.race;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RaceOrderRepository extends JpaRepository<RaceOrderEntity, Integer> {

    @Query("""
            SELECT DISTINCT ro
            FROM RaceOrderEntity ro
            JOIN RaceResultEntity rr ON ro.raceId = rr.id.raceId
            WHERE ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearJoinWithRaceResults(int year);

    @Query("""
            SELECT DISTINCT ro
            FROM RaceOrderEntity ro
            JOIN StartingGridEntity sg ON ro.raceId = sg.id.raceId
            WHERE ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearJoinWithStartingGrid(int year);

    @Query("""
            SELECT DISTINCT ro
            FROM RaceOrderEntity ro
            JOIN DriverStandingsEntity ds on ds.id.raceId = ro.raceId
            JOIN ConstructorStandingsEntity cs on cs.id.raceId = ro.raceId
            WHERE ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearJoinWithStandings(int year);

    @Query("""
            SELECT ro
            FROM RaceOrderEntity ro
            WHERE ro.raceId NOT IN (SELECT DISTINCT rr.id.raceId FROM RaceResultEntity rr)
            AND ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearNotInRaceResult(int year);

    @Query("""
            SELECT ro
            FROM RaceOrderEntity ro
            WHERE ro.raceId NOT IN (SELECT rr.id.raceId FROM RaceResultEntity rr)
            AND ro.year NOT IN (SELECT yf.year FROM YearFinishedEntity yf)
            ORDER BY ro.year, ro.position
            """)
    List<RaceOrderEntity> findAllByNotFinished();

    List<RaceOrderEntity> findAllByYearOrderByPosition(int year);

    boolean existsByRaceIdAndYear(int raceId, int year);

    Optional<RaceOrderEntity> findTopByYearOrderByPositionDesc(int year);

    @Modifying
    @Query("""
       UPDATE RaceOrderEntity ro
       SET ro.position = :position
       WHERE ro.raceId = :raceId
       AND ro.year = :year
       """)
    void updatePosition(int raceId, int year, int position);
}
