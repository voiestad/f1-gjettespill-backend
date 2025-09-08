package no.vebb.f1.guessing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConstructorGuessRepository extends JpaRepository<ConstructorGuessEntity, CompetitorGuessId> {
    List<ConstructorGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(int year, UUID userId);
    List<ConstructorGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
}
