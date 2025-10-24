package no.voiestad.f1.guessing.driverPlace;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.guessing.category.Category;
import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.guessing.collection.IPlaceGuessData;
import no.voiestad.f1.guessing.collection.IUserRaceGuess;
import no.voiestad.f1.guessing.collection.IUserRaceGuessTable;
import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DriverPlaceGuessRepository extends JpaRepository<DriverPlaceGuessEntity, DriverPlaceGuessId> {
    @Query("""
            SELECT dpg.id.categoryName AS category, dpg.driver.driverName AS driver, r.raceName AS raceName, r.year AS year
            FROM DriverPlaceGuessEntity dpg
            JOIN RaceEntity r ON dpg.id.raceId = r.raceId
            WHERE dpg.id.userId = :userId
            ORDER BY r.year DESC, r.position, dpg.id.categoryName
            """)
    List<IPlaceGuessData> findAllByUserId(UUID userId);

    @Query("""
            SELECT u.username AS username, dpg.driver.driverName AS driverName, sg.position AS startPosition
            FROM DriverPlaceGuessEntity dpg
            JOIN UserEntity u ON u.id = dpg.id.userId
            JOIN StartingGridEntity sg ON sg.id.raceId = dpg.id.raceId AND sg.id.driver.driverId = dpg.driver.driverId
            WHERE dpg.id.raceId = :raceId AND dpg.id.categoryName = :categoryName
            ORDER BY u.username
            """)
    List<IUserRaceGuess> findAllByRaceIdAndCategoryNameOrderByUsername(Category categoryName, RaceId raceId);
    @Query("""
            SELECT r.position as racePosition, r.raceName AS raceName, dpg.driver.driverName AS driverName, sg.position AS startPosition, rr.id.finishingPosition AS finishingPosition
            FROM DriverPlaceGuessEntity dpg
            JOIN RaceEntity r ON r.raceId = dpg.id.raceId
            JOIN StartingGridEntity sg ON sg.id.raceId = r.raceId AND dpg.driver.driverId = sg.id.driver.driverId
            JOIN RaceResultEntity rr ON rr.id.raceId = r.raceId AND dpg.driver.driverId = rr.driver.driverId
            WHERE dpg.id.categoryName = :categoryName AND dpg.id.userId = :userId AND r.year = :year AND r.position <= :position
            ORDER BY r.position
            """)
    List<IUserRaceGuessTable> findAllByCategoryNameAndYearAndPositionAndUserIdOrderByPosition(Category categoryName, Year year, RacePosition position, UUID userId);
}
