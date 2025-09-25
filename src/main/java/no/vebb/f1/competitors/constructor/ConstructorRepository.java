package no.vebb.f1.competitors.constructor;

import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConstructorRepository extends JpaRepository<ConstructorEntity, ConstructorId> {
    List<ConstructorEntity> findAllByYearOrderByPosition(Year year);
    Optional<ConstructorEntity> findByConstructorNameAndYear(Constructor constructorName, Year year);
}
