package no.vebb.f1.competitors;

import no.vebb.f1.results.IColoredCompetitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConstructorYearRepository extends JpaRepository<ConstructorYearEntity, ConstructorId> {
    List<ConstructorYearEntity> findAllByIdYearOrderByPosition(int year);

    boolean existsByIdConstructorName(String constructorName);

    @Modifying
    @Query("""
       UPDATE ConstructorYearEntity cy
       SET cy.position = :position
       WHERE cy.id.constructorName = :constructorName
       AND cy.id.year = :year
       """)
    void updatePosition(String constructorName, int year, int position);

    @Query("""
            SELECT cy.id.constructorName as constructor, cc.color as color
            FROM ConstructorYearEntity cy
            LEFT JOIN ConstructorColorEntity cc ON cc.id.constructorName = cy.id.constructorName
            WHERE cy.id.year = :year
            ORDER BY cy.position
            """)
    List<IColoredCompetitor> findAllByYearOrderByPosition(int year);
}
