package no.voiestad.f1.results.raceResult;

import java.util.List;

import no.voiestad.f1.race.RaceId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceResultRepository extends JpaRepository<RaceResultEntity, RaceResultId> {
    List<RaceResultEntity> findAllByIdRaceIdOrderByIdFinishingPosition(RaceId raceId);

    boolean existsByIdRaceId(RaceId raceId);
}
