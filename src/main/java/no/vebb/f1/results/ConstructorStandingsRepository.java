package no.vebb.f1.results;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConstructorStandingsRepository extends JpaRepository<ConstructorStandingsEntity, ConstructorStandingsId> {
    List<ConstructorStandingsEntity> findAllByIdRaceIdOrderByPosition(int raceId);
}
