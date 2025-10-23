package no.voiestad.f1.scoring.diffPointsMap;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiffPointsMapRepository extends JpaRepository<DiffPointsMapEntity, DiffPointsMapId> {
    List<DiffPointsMapEntity> findAllByIdYearAndIdCategoryNameOrderByIdDiff(Year year, Category category);
}
