package no.vebb.f1.year;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YearRepository extends JpaRepository<YearEntity, Integer> {
    List<YearEntity> findAllByOrderByYearDesc();
}
