package no.voiestad.f1.guessing.driver;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.guessing.constructor.CompetitorGuessId;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverGuessRepository extends JpaRepository<DriverGuessEntity, CompetitorGuessId> {
    List<DriverGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(Year year, UUID userId);
    List<DriverGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
}
