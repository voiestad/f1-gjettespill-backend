package no.voiestad.f1.placement.placementCategory;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacementCategoryYearStartRepository extends JpaRepository<PlacementCategoryYearStartEntity, PlacementCategoryYearStartId> {
    List<PlacementCategoryYearStartEntity> findByIdYearAndIdUserId(Year year, UUID userId);
}
