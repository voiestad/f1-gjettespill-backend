package no.vebb.f1.competitors.driver;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverAlternativeNameRepository extends JpaRepository<DriverAlternativeNameEntity, DriverAlternativeNameId> {
    List<DriverAlternativeNameEntity> findAllByIdYear(Year year);
}
