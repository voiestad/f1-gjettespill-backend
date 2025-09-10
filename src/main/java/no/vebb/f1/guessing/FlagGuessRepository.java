package no.vebb.f1.guessing;

import no.vebb.f1.race.RacePosition;
import no.vebb.f1.util.collection.IFlagGuessed;
import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FlagGuessRepository extends JpaRepository<FlagGuessEntity, FlagGuessId> {
    List<FlagGuessEntity> findAllByIdUserIdOrderByIdYearDescIdFlagName(UUID userId);
    List<FlagGuessEntity> findAllByIdUserIdAndIdYear(UUID userId, Year year);

    @Query("""
            SELECT fg.id.flagName AS flagName, fg.amount AS guessed, 0 AS actual
            FROM FlagGuessEntity fg
            WHERE fg.id.year = :year AND fg.id.userId = :userId
            GROUP BY fg.id.flagName, fg.amount
            """)
    List<IFlagGuessed> findAllByUserIdAndYear(UUID userId, Year year);

    @Query("""
            SELECT fg.id.flagName AS flagName, fg.amount AS guessed, COALESCE(COUNT(fs.flagName), 0) AS actual
            FROM FlagGuessEntity fg
            JOIN RaceOrderEntity ro ON fg.id.year = ro.year
            LEFT JOIN FlagStatEntity fs ON fs.flagName = fg.id.flagName AND fs.raceId = ro.raceId
            WHERE ro.year = :year AND fg.id.userId = :userId AND ro.position <= :position
            GROUP BY fg.id.flagName, fg.amount
            """)
    List<IFlagGuessed> findAllByUserIdAndYearAndPosition(UUID userId, Year year, RacePosition position);

}
