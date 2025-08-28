package no.vebb.f1.results;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConstructorStandingsRepository extends JpaRepository<ConstructorStandingsEntity, ConstructorStandingsId> {
    @Query(value = "SELECT * FROM constructor_standings WHERE race_id = :raceId ORDER BY position", nativeQuery = true)
    List<ConstructorStandingsEntity> findAllByRaceIdOrderByPosition(int raceId);
}
