package no.vebb.f1.stats.flag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FlagRepository extends JpaRepository<FlagEntity, String> {
    @Query("""
            SELECT ro.year
            FROM RaceOrderEntity ro
            JOIN FlagStatEntity fs ON ro.raceId = fs.raceId
            WHERE fs.flagId = :flagId
            """)
    Optional<Integer> findYearByFlagId(int flagId);
}
