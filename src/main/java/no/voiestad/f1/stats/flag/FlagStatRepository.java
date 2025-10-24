package no.voiestad.f1.stats.flag;

import java.util.List;
import java.util.Optional;

import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
