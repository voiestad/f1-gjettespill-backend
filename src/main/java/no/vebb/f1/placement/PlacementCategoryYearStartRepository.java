package no.vebb.f1.placement;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlacementCategoryYearStartRepository extends JpaRepository<PlacementCategoryYearStartEntity, PlacementCategoryYearStartId> {
    List<PlacementCategoryYearStartEntity> findByIdYearAndIdUserId(Year year, UUID userId);
}
