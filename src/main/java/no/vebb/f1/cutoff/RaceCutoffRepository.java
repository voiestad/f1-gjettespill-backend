package no.vebb.f1.cutoff;

import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceCutoffRepository extends JpaRepository<RaceCutoffEntity, RaceId> {
    List<RaceCutoffEntity> findAllByRaceOrderYearOrderByRaceOrderPosition(Year year);
}
