package no.vebb.f1.competitors;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverTeamRepository extends JpaRepository<DriverTeamEntity, DriverId> {
    List<DriverTeamEntity> findAllByIdYearOrderByDriverYearPosition(int idYear);
}
