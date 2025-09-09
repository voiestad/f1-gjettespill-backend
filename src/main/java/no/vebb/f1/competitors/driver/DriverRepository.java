package no.vebb.f1.competitors.driver;

import no.vebb.f1.competitors.domain.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<DriverEntity, Driver> {
}
