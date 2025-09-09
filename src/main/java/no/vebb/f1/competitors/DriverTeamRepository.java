package no.vebb.f1.competitors;

import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverTeamRepository extends JpaRepository<DriverTeamEntity, DriverId> {
    List<DriverTeamEntity> findAllByIdYearOrderByDriverYearPosition(Year year);
}
