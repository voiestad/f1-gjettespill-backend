package no.vebb.f1.placement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlacementCategoryRepository extends JpaRepository<PlacementCategoryEntity, PlacementCategoryId> {
    List<PlacementCategoryEntity> findByIdRaceIdAndIdUserId(int raceId, UUID userId);
}
