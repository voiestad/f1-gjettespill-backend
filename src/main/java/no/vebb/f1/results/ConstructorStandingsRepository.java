package no.vebb.f1.results;

import no.vebb.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConstructorStandingsRepository extends JpaRepository<ConstructorStandingsEntity, ConstructorStandingsId> {
    List<ConstructorStandingsEntity> findAllByIdRaceIdOrderByPosition(RaceId raceId);
}
