package no.vebb.f1.placement.placementYear;

import no.vebb.f1.placement.domain.UserPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlacementYearRepository extends JpaRepository<PlacementYearEntity, PlacementYearId> {
    int countByPlacementAndIdUserId(UserPosition placement, UUID userId);

    List<PlacementYearEntity> findByIdUserIdOrderByPlacementDesc(UUID userId);
}
