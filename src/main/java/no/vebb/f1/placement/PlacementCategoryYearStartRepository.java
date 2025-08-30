package no.vebb.f1.placement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlacementCategoryYearStartRepository extends JpaRepository<PlacementCategoryYearStartEntity, PlacementCategoryYearStartId> {
    List<PlacementCategoryYearStartEntity> findByIdYearAndIdUserId(int year, UUID userId);
}
