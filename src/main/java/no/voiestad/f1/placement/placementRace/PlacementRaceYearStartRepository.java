package no.voiestad.f1.placement.placementRace;

import java.util.Optional;
import java.util.UUID;

import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacementRaceYearStartRepository extends JpaRepository<PlacementRaceYearStartEntity, PlacementRaceYearStartId> {
    Optional<PlacementRaceYearStartEntity> findByIdYearAndIdUserId(Year year, UUID userId);
}
