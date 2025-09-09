package no.vebb.f1.stats;

import no.vebb.f1.util.domainPrimitive.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

    public interface FlagStatRepository extends JpaRepository<FlagStatEntity, Integer> {
    List<FlagStatEntity> findAllByRaceIdOrderBySessionTypeAscRoundAsc(RaceId raceId);
}
