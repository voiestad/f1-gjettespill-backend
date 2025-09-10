package no.vebb.f1.scoring;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiffPointsMapRepository extends JpaRepository<DiffPointsMapEntity, DiffPointsMapId> {
    List<DiffPointsMapEntity> findAllByIdYearAndIdCategoryNameOrderByIdDiff(Year year, Category category);
}
