package no.vebb.f1.mail;

import no.vebb.f1.util.domainPrimitive.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MailingListRepository extends JpaRepository<MailingListEntity, UUID> {
    @Query("""
        SELECT ml
        FROM MailingListEntity ml
        WHERE ml.userId NOT IN
            (SELECT dpg.id.userId FROM DriverPlaceGuessEntity dpg WHERE dpg.id.raceId = :raceId GROUP BY dpg.id.userId HAVING COUNT(*) = 2)
    """)
    List<MailingListEntity> findAllByRaceId(RaceId raceId);
}
