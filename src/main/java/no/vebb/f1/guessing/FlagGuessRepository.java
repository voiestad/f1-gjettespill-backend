package no.vebb.f1.guessing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FlagGuessRepository extends JpaRepository<FlagGuessEntity, FlagGuessId> {
}
