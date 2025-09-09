package no.vebb.f1.results;

import no.vebb.f1.util.domainPrimitive.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverStandingsRepository extends JpaRepository<DriverStandingsEntity, DriverStandingsId> {
    List<DriverStandingsEntity> findAllByIdRaceIdOrderByPosition(RaceId raceId);
}
