package no.voiestad.f1.year;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface YearRepository extends JpaRepository<YearEntity, Year> {
    List<YearEntity> findAllByOrderByYearDesc();
}
