package no.vebb.f1.results;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverStandingsRepository extends JpaRepository<DriverStandingsEntity, DriverStandingsId> {
    @Query(value = "SELECT * FROM driver_standings WHERE race_id = :raceId ORDER BY position", nativeQuery = true)
    List<DriverStandingsEntity> findAllByRaceIdOrderByPosition(int raceId);
}
