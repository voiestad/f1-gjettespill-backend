package no.vebb.f1.scoring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiffPointsMapRepository extends JpaRepository<DiffPointsMapEntity, DiffPointsMapId> {
    List<DiffPointsMapEntity> findAllByIdYearAndIdCategoryNameOrderByIdDiff(int year, String category);
}
