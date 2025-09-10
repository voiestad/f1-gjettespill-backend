package no.vebb.f1.stats.flag;

import no.vebb.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

    public interface FlagStatRepository extends JpaRepository<FlagStatEntity, Integer> {
    List<FlagStatEntity> findAllByRaceIdOrderBySessionTypeAscRoundAsc(RaceId raceId);
}
