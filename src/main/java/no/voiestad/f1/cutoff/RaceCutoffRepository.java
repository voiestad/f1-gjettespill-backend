package no.voiestad.f1.cutoff;

import java.util.List;

import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceCutoffRepository extends JpaRepository<RaceCutoffEntity, RaceId> {
    List<RaceCutoffEntity> findAllByRaceYearOrderByRacePosition(Year year);
}
