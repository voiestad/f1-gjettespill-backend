package no.vebb.f1.placement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlacementRaceYearStartRepository extends JpaRepository<PlacementRaceYearStartEntity, PlacementRaceYearStartId> {
    Optional<PlacementRaceYearStartEntity> findByIdYearAndIdUserId(int year, UUID userId);
}
