package no.vebb.f1.guessing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverGuessRepository extends JpaRepository<DriverGuessEntity, CompetitorGuessId> {
    List<DriverGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(int year, UUID userId);
    List<DriverGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
}
