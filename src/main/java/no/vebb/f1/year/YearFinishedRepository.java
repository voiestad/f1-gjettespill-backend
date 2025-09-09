package no.vebb.f1.year;

import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YearFinishedRepository extends JpaRepository<YearFinishedEntity, Year> {
}
