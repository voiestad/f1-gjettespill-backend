package no.vebb.f1.results;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverStandingsRepository extends JpaRepository<DriverStandingsEntity, DriverStandingsId> {
    List<DriverStandingsEntity> findAllByIdRaceIdOrderByPosition(int raceId);
}
