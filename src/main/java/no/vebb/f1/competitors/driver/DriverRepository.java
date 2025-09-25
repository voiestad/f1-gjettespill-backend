package no.vebb.f1.competitors.driver;

import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverEntity, DriverId> {
    List<DriverEntity> findAllByYearOrderByPosition(Year year);
    Optional<DriverEntity> findByDriverNameAndYear(Driver driverName, Year year);
}
