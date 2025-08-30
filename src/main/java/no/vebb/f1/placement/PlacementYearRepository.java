package no.vebb.f1.placement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlacementYearRepository extends JpaRepository<PlacementYearEntity, PlacementYearId> {
    int countByPlacementAndIdUserId(int placement, UUID userId);

    List<PlacementYearEntity> findByIdUserId(UUID userId);
}
