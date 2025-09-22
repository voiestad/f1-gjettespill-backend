package no.vebb.f1.stats.flag;

import no.vebb.f1.race.RaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FlagStatRepository extends JpaRepository<FlagStatEntity, Integer> {
    List<FlagStatEntity> findAllByRaceIdOrderBySessionTypeAscRoundAsc(RaceId raceId);
    @Query("""
            SELECT ro.year
            FROM RaceOrderEntity ro
            JOIN FlagStatEntity fs ON ro.raceId = fs.raceId
            WHERE fs.flagId = :flagId
            """)
    Optional<Integer> findYearByFlagId(int flagId);
    }
