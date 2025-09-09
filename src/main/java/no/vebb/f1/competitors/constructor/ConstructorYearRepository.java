package no.vebb.f1.competitors.constructor;

import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.results.IColoredCompetitor;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConstructorYearRepository extends JpaRepository<ConstructorYearEntity, ConstructorId> {
    List<ConstructorYearEntity> findAllByIdYearOrderByPosition(Year year);

    boolean existsByIdConstructorName(Constructor constructorName);

    @Query("""
            SELECT cy.id.constructorName as constructor, cc.color as color
            FROM ConstructorYearEntity cy
            LEFT JOIN ConstructorColorEntity cc ON cc.id.constructorName = cy.id.constructorName
            WHERE cy.id.year = :year
            ORDER BY cy.position
            """)
    List<IColoredCompetitor> findAllByYearOrderByPosition(Year year);
}
