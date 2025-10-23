package no.voiestad.f1.placement.placementCategory;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.race.RaceId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacementCategoryRepository extends JpaRepository<PlacementCategoryEntity, PlacementCategoryId> {
    List<PlacementCategoryEntity> findByIdRaceIdAndIdUserId(RaceId raceId, UUID userId);
}
