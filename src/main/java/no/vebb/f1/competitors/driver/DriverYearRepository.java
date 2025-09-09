package no.vebb.f1.competitors.driver;

import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.results.IColoredCompetitor;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverYearRepository extends JpaRepository<DriverYearEntity, DriverId> {
    List<DriverYearEntity> findAllByIdYearOrderByPosition(Year year);

    boolean existsByIdDriverName(Driver driverName);

    @Query("""
            SELECT dy.id.driverName as competitorName, cc.color as color
            FROM DriverYearEntity dy
            LEFT JOIN DriverTeamEntity dt ON dt.id.driverName = dy.id.driverName
            LEFT JOIN ConstructorColorEntity cc ON cc.id.constructorName = dt.team
            WHERE dy.id.year = :year
            ORDER BY dy.position
            """)
    List<IColoredCompetitor> findAllByYearOrderByPositionWithColor(Year year);



}
