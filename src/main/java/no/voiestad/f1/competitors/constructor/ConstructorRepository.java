package no.voiestad.f1.competitors.constructor;

import java.util.List;
import java.util.Optional;

import no.voiestad.f1.competitors.domain.ConstructorName;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConstructorRepository extends JpaRepository<ConstructorEntity, ConstructorId> {
    @Query("""
        SELECT c
        FROM ConstructorEntity c
        LEFT JOIN ConstructorStandingsEntity cs ON c.constructorId = cs.id.constructor.constructorId
            AND cs.id.raceId = :raceId
        WHERE c.year = :year
        ORDER BY cs.position, c.position
    """)
    List<ConstructorEntity> findAllByYearOrderByStandings(Year year, RaceId raceId);
    List<ConstructorEntity> findAllByYearOrderByPosition(Year year);
    Optional<ConstructorEntity> findByConstructorNameAndYear(ConstructorName constructorName, Year year);
    @Query(value = "SELECT NEXTVAL('constructors_constructor_id_seq')", nativeQuery = true)
    int getNextId();
}
