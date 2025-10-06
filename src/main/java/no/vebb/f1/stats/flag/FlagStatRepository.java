package no.vebb.f1.stats.flag;

import no.vebb.f1.race.RaceId;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FlagStatRepository extends JpaRepository<FlagStatEntity, Integer> {
    List<FlagStatEntity> findAllByRaceIdOrderBySessionTypeAscRoundAsc(RaceId raceId);
    @Query("""
            SELECT r.year
            FROM RaceEntity r
            JOIN FlagStatEntity fs ON r.raceId = fs.raceId
            WHERE fs.flagId = :flagId
            """)
    Optional<Year> findYearByFlagId(int flagId);
    }
