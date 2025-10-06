package no.vebb.f1.cutoff;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceCutoffRepository extends JpaRepository<RaceCutoffEntity, RaceId> {
    List<RaceCutoffEntity> findAllByRaceYearOrderByRacePosition(Year year);
}
