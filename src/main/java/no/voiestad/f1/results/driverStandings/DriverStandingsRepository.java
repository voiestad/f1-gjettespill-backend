package no.voiestad.f1.results.driverStandings;

import java.util.List;

import no.voiestad.f1.race.RaceId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;


public interface DriverStandingsRepository extends JpaRepository<DriverStandingsEntity, DriverStandingsId> {
    List<DriverStandingsEntity> findAllByIdRaceIdOrderByPosition(RaceId raceId);
    @Modifying
    void deleteAllByIdRaceId(RaceId raceId);
}
