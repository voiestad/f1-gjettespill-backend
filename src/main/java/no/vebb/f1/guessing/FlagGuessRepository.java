package no.vebb.f1.guessing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FlagGuessRepository extends JpaRepository<FlagGuessEntity, FlagGuessId> {
    List<FlagGuessEntity> findAllByIdUserIdOrderByIdYearDescIdFlagName(UUID userId);
    List<FlagGuessEntity> findAllByIdUserIdAndIdYear(UUID userId, int year);
}
