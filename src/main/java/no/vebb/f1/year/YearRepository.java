package no.vebb.f1.year;

import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YearRepository extends JpaRepository<YearEntity, Year> {
    List<YearEntity> findAllByOrderByYearDesc();
}
