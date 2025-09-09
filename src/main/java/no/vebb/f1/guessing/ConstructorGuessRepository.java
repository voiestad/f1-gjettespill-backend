package no.vebb.f1.guessing;

import no.vebb.f1.results.IColoredCompetitor;
import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ConstructorGuessRepository extends JpaRepository<ConstructorGuessEntity, CompetitorGuessId> {
    List<ConstructorGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(Year year, UUID userId);
    List<ConstructorGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
    @Query("""
            SELECT cg.constructorName as competitorName, cc.color as color
            FROM ConstructorGuessEntity cg
            LEFT JOIN ConstructorColorEntity cc ON cc.id.constructorName = cg.constructorName
            WHERE cg.id.userId = :userId AND cg.id.year = :year
            ORDER BY cg.id.position
            """)
    List<IColoredCompetitor> findAllByUserIdOrderByIdPosition(UUID userId, Year year);
}
