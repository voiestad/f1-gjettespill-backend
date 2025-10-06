package no.vebb.f1.notification.notified;

import no.vebb.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotifiedRepository extends JpaRepository<NotifiedEntity, Integer> {

    int countAllByRaceIdAndUserId(RaceId raceId, UUID userId);
    
    void deleteByUserId(UUID userId);

    @Query("""
        SELECT r.raceName AS raceName, count(*) as timesNotified, r.year AS year
        FROM NotifiedEntity n
        JOIN RaceEntity r ON r.raceId = n.raceId
        WHERE n.userId = :userId
        GROUP BY n.userId, r.position, r.year, r.raceName
        ORDER BY r.year DESC, r.position
    """)
    List<IUserNotifiedCount> findAllByUserId(UUID userId);
}
