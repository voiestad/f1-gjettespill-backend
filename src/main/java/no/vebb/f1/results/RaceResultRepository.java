package no.vebb.f1.results;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RaceResultRepository extends JpaRepository<RaceResultEntity, RaceResultId> {
    @Query(value = "SELECT * FROM race_results WHERE race_id = :raceId ORDER BY finishing_position", nativeQuery = true)
    List<RaceResultEntity> findAllByRaceIdOrderByPosition(int raceId);

    @Query(value = "SELECT EXISTS (SELECT * from race_results WHERE race_id = :raceId)", nativeQuery = true)
    boolean existsByRaceId(int raceId);
}
