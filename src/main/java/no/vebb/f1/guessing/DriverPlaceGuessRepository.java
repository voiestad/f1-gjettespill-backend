package no.vebb.f1.guessing;

import no.vebb.f1.util.collection.IPlaceGuess;
import no.vebb.f1.util.collection.IUserRaceGuess;
import no.vebb.f1.util.collection.IUserRaceGuessTable;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DriverPlaceGuessRepository extends JpaRepository<DriverPlaceGuessEntity, DriverPlaceGuessId> {
    @Query("""
            SELECT dpg.id.categoryName AS category, dpg.driverName AS driver, r.raceName AS raceName, ro.year AS year
            FROM DriverPlaceGuessEntity dpg
            JOIN RaceEntity r ON dpg.id.raceId = r.raceId
            JOIN RaceOrderEntity ro ON dpg.id.raceId = ro.raceId
            WHERE dpg.id.userId = :userId
            ORDER BY ro.year DESC, ro.position, dpg.id.categoryName
            """)
    List<IPlaceGuess> findAllByUserId(UUID userId);

    @Query("""
            SELECT u.username AS username, dpg.driverName AS driverName, sg.position AS startPosition
            FROM DriverPlaceGuessEntity dpg
            JOIN UserEntity u ON u.id = dpg.id.userId
            JOIN StartingGridEntity sg ON sg.id.raceId = dpg.id.raceId AND sg.id.driverName = dpg.driverName
            WHERE dpg.id.raceId = :raceId AND dpg.id.categoryName = :categoryName
            ORDER BY u.username
            """)
    List<IUserRaceGuess> findAllByRaceIdAndCategoryNameOrderByUsername(String categoryName, RaceId raceId);
    @Query("""
            SELECT ro.position as racePosition, r.raceName AS raceName, dpg.driverName AS driverName, sg.position AS startPosition, rr.id.finishingPosition AS finishingPosition
            FROM DriverPlaceGuessEntity dpg
            JOIN RaceEntity r ON r.raceId = dpg.id.raceId
            JOIN RaceOrderEntity ro ON r.raceId = ro.raceId
            JOIN StartingGridEntity sg ON sg.id.raceId = r.raceId AND dpg.driverName = sg.id.driverName
            JOIN RaceResultEntity rr ON rr.id.raceId = r.raceId AND dpg.driverName = rr.driverName
            WHERE dpg.id.categoryName = :categoryName AND dpg.id.userId = :userId AND ro.year = :year AND ro.position <= :position
            ORDER BY ro.position
            """)
    List<IUserRaceGuessTable> findAllByCategoryNameAndYearAndPositionAndUserIdOrderByPosition(String categoryName, Year year, int position, UUID userId);
}
