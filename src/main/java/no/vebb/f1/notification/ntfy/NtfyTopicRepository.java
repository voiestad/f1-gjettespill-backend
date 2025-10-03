package no.vebb.f1.notification.ntfy;

import no.vebb.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NtfyTopicRepository extends JpaRepository<NtfyTopicEntity, UUID> {
    @Query("""
        SELECT nt
        FROM NtfyTopicEntity nt
        WHERE nt.userId NOT IN
            (SELECT dpg.id.userId FROM DriverPlaceGuessEntity dpg WHERE dpg.id.raceId = :raceId GROUP BY dpg.id.userId HAVING COUNT(*) = 2)
    """)
    List<NtfyTopicEntity> findAllByRaceId(RaceId raceId);
}
