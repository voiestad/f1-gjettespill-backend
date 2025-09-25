package no.vebb.f1.guessing.constructor;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConstructorGuessRepository extends JpaRepository<ConstructorGuessEntity, CompetitorGuessId> {
    List<ConstructorGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(Year year, UUID userId);
    List<ConstructorGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
}
