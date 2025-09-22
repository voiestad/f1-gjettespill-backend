package no.vebb.f1.race;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RaceOrderRepository extends JpaRepository<RaceOrderEntity, RaceId> {

    @Query("""
            SELECT DISTINCT ro
            FROM RaceOrderEntity ro
            JOIN RaceResultEntity rr ON ro.raceId = rr.id.raceId
            WHERE ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearJoinWithRaceResults(Year year);

    @Query("""
            SELECT DISTINCT ro
            FROM RaceOrderEntity ro
            JOIN StartingGridEntity sg ON ro.raceId = sg.id.raceId
            WHERE ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearJoinWithStartingGrid(Year year);

    @Query("""
            SELECT DISTINCT ro
            FROM RaceOrderEntity ro
            JOIN DriverStandingsEntity ds on ds.id.raceId = ro.raceId
            JOIN ConstructorStandingsEntity cs on cs.id.raceId = ro.raceId
            WHERE ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearJoinWithStandings(Year year);

    @Query("""
            SELECT ro
            FROM RaceOrderEntity ro
            WHERE ro.raceId NOT IN (SELECT DISTINCT rr.id.raceId FROM RaceResultEntity rr)
            AND ro.year = :year
            ORDER BY ro.position
            """)
    List<RaceOrderEntity> findAllByYearNotInRaceResult(Year year);

    @Query("""
            SELECT ro
            FROM RaceOrderEntity ro
            WHERE ro.raceId NOT IN (SELECT rr.id.raceId FROM RaceResultEntity rr)
            AND ro.year NOT IN (SELECT yf.year FROM YearFinishedEntity yf)
            ORDER BY ro.year, ro.position
            """)
    List<RaceOrderEntity> findAllByNotFinished();

    List<RaceOrderEntity> findAllByYearOrderByPosition(Year year);

    boolean existsByRaceIdAndYear(RaceId raceId, Year year);

    Optional<RaceOrderEntity> findTopByYearOrderByPositionDesc(Year year);
}
