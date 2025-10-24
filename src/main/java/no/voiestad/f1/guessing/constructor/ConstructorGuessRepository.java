package no.voiestad.f1.guessing.constructor;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstructorGuessRepository extends JpaRepository<ConstructorGuessEntity, CompetitorGuessId> {
    List<ConstructorGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(Year year, UUID userId);
    List<ConstructorGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
}
