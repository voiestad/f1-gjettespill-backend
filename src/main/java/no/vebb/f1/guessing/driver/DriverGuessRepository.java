package no.vebb.f1.guessing.driver;

import no.vebb.f1.guessing.constructor.CompetitorGuessId;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverGuessRepository extends JpaRepository<DriverGuessEntity, CompetitorGuessId> {
    List<DriverGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(Year year, UUID userId);
    List<DriverGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
}
