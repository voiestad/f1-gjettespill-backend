package no.voiestad.f1.competitors.driver;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverTeamRepository extends JpaRepository<DriverTeamEntity, DriverId> {
}
