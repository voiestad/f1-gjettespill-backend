package no.vebb.f1.competitors;

import no.vebb.f1.results.IColoredCompetitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverYearRepository extends JpaRepository<DriverYearEntity, DriverId> {
    List<DriverYearEntity> findAllByIdYearOrderByPosition(int year);

    @Modifying
    @Query("""
       UPDATE DriverYearEntity dy
       SET dy.position = :position
       WHERE dy.id.driverName = :driverName
       AND dy.id.year = :year
       """)
    void updatePosition(String driverName, int year, int position);

    boolean existsByIdDriverName(String driverName);

    @Query("""
            SELECT dy.id.driverName as competitorName, cc.color as color
            FROM DriverYearEntity dy
            LEFT JOIN DriverTeamEntity dt ON dt.id.driverName = dy.id.driverName
            LEFT JOIN ConstructorColorEntity cc ON cc.id.constructorName = dt.team
            WHERE dy.id.year = :year
            ORDER BY dy.position
            """)
    List<IColoredCompetitor> findAllByYearOrderByPosition(int year);
}
