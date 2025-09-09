package no.vebb.f1.placement;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlacementRaceYearStartRepository extends JpaRepository<PlacementRaceYearStartEntity, PlacementRaceYearStartId> {
    Optional<PlacementRaceYearStartEntity> findByIdYearAndIdUserId(Year year, UUID userId);
}
