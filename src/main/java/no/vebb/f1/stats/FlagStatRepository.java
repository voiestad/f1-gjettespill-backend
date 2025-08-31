package no.vebb.f1.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlagStatRepository extends JpaRepository<FlagStatEntity, Integer> {
    List<FlagStatEntity> findAllByRaceIdOrderBySessionTypeAscRoundAsc(int raceId);
}
