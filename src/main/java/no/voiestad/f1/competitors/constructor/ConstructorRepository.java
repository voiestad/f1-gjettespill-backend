package no.voiestad.f1.competitors.constructor;

import java.util.List;
import java.util.Optional;

import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConstructorRepository extends JpaRepository<ConstructorEntity, ConstructorId> {
    List<ConstructorEntity> findAllByYearOrderByPosition(Year year);
    Optional<ConstructorEntity> findByConstructorNameAndYear(ConstructorName constructorName, Year year);
    @Query(value = "SELECT NEXTVAL('constructors_constructor_id_seq')", nativeQuery = true)
    int getNextId();
}
