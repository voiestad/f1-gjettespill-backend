package no.vebb.f1.results;

import no.vebb.f1.util.domainPrimitive.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceResultRepository extends JpaRepository<RaceResultEntity, RaceResultId> {
    List<RaceResultEntity> findAllByIdRaceIdOrderByIdFinishingPosition(RaceId raceId);

    boolean existsByIdRaceId(RaceId raceId);
}
