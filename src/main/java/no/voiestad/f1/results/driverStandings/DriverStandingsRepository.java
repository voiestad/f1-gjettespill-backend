package no.voiestad.f1.results.driverStandings;

import java.util.List;

import no.voiestad.f1.race.RaceId;

import org.springframework.data.jpa.repository.JpaRepository;


public interface DriverStandingsRepository extends JpaRepository<DriverStandingsEntity, DriverStandingsId> {
    List<DriverStandingsEntity> findAllByIdRaceIdOrderByPosition(RaceId raceId);
}
