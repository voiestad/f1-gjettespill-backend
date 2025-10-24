package no.voiestad.f1.placement.placementYear;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.placement.domain.UserPosition;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacementYearRepository extends JpaRepository<PlacementYearEntity, PlacementYearId> {
    int countByPlacementAndIdUserId(UserPosition placement, UUID userId);

    List<PlacementYearEntity> findByIdUserIdOrderByPlacementDesc(UUID userId);
}
