package no.vebb.f1.cutoff;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YearCutoffRepository extends JpaRepository<YearCutoffEntity, Year> {
}
