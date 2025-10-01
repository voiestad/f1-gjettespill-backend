package no.vebb.f1.competitors.constructor;

import no.vebb.f1.competitors.domain.ConstructorName;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConstructorRepository extends JpaRepository<ConstructorEntity, ConstructorId> {
    List<ConstructorEntity> findAllByYearOrderByPosition(Year year);
    Optional<ConstructorEntity> findByConstructorNameAndYear(ConstructorName constructorName, Year year);
    @Query(value = "SELECT NEXTVAL('constructors_constructor_id_seq')", nativeQuery = true)
    int getNextId();
}
