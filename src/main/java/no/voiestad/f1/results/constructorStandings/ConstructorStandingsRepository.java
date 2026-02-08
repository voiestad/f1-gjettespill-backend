package no.voiestad.f1.results.constructorStandings;

import java.util.List;

import no.voiestad.f1.race.RaceId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface ConstructorStandingsRepository extends JpaRepository<ConstructorStandingsEntity, ConstructorStandingsId> {
    List<ConstructorStandingsEntity> findAllByIdRaceIdOrderByPosition(RaceId raceId);
    @Modifying
    void deleteAllByIdRaceId(RaceId raceId);
}
