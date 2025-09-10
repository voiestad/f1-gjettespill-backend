package no.vebb.f1.results.raceResult;

import no.vebb.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceResultRepository extends JpaRepository<RaceResultEntity, RaceResultId> {
    List<RaceResultEntity> findAllByIdRaceIdOrderByIdFinishingPosition(RaceId raceId);

    boolean existsByIdRaceId(RaceId raceId);
}
