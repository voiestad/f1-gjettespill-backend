package no.vebb.f1.guessing;

import no.vebb.f1.results.IColoredCompetitor;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DriverGuessRepository extends JpaRepository<DriverGuessEntity, CompetitorGuessId> {
    List<DriverGuessEntity> findAllByIdYearAndIdUserIdOrderByIdPosition(Year year, UUID userId);
    List<DriverGuessEntity> findAllByIdUserIdOrderByIdYearDescIdPosition(UUID userId);
    @Query("""
                SELECT dg.driverName as competitorName, cc.color as color
                FROM DriverGuessEntity dg
                LEFT JOIN DriverTeamEntity dt ON dt.id.driverName = dg.driverName
                LEFT JOIN ConstructorColorEntity cc ON cc.id.constructorName = dt.team
                WHERE dg.id.userId = :userId AND dg.id.year = :year
                ORDER BY dg.id.position
                """)
    List<IColoredCompetitor> findAllByUserIdOrderByIdPosition(UUID userId, Year year);
}
