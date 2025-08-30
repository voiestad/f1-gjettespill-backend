package no.vebb.f1.results;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceResultRepository extends JpaRepository<RaceResultEntity, RaceResultId> {
    List<RaceResultEntity> findAllByIdRaceIdOrderByIdFinishingPosition(int raceId);

    boolean existsByIdRaceId(int raceId);
}
