package no.voiestad.f1.competitors.driver;

import java.util.List;
import java.util.Optional;

import no.voiestad.f1.competitors.domain.DriverName;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DriverRepository extends JpaRepository<DriverEntity, DriverId> {
    @Query("""
        SELECT d
        FROM DriverEntity d
        LEFT JOIN DriverStandingsEntity ds ON d.driverId = ds.id.driver.driverId
            AND ds.id.raceId = :raceId
        WHERE d.year = :year
        ORDER BY ds.position, d.position
    """)
    List<DriverEntity> findAllByYearOrderByStandings(Year year, RaceId raceId);
    List<DriverEntity> findAllByYearOrderByPosition(Year year);
    Optional<DriverEntity> findByDriverNameAndYear(DriverName driverName, Year year);
    @Query(value = "SELECT NEXTVAL('drivers_driver_id_seq')", nativeQuery = true)
    int getNextId();
}
