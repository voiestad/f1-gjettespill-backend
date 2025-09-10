package no.vebb.f1.placement.placementCategory;

import no.vebb.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlacementCategoryRepository extends JpaRepository<PlacementCategoryEntity, PlacementCategoryId> {
    List<PlacementCategoryEntity> findByIdRaceIdAndIdUserId(RaceId raceId, UUID userId);
}
