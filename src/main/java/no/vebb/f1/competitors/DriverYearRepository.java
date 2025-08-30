package no.vebb.f1.competitors;

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
}
