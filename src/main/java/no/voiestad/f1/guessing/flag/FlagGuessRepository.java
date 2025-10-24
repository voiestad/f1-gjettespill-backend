package no.voiestad.f1.guessing.flag;

import java.util.List;
import java.util.UUID;

import no.voiestad.f1.race.RacePosition;
import no.voiestad.f1.guessing.collection.IFlagGuessed;
import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
            JOIN RaceEntity r ON fg.id.year = r.year
            LEFT JOIN FlagStatEntity fs ON fs.flagName = fg.id.flagName AND fs.raceId = r.raceId
            WHERE r.year = :year AND fg.id.userId = :userId AND r.position <= :position
            GROUP BY fg.id.flagName, fg.amount
            """)
    List<IFlagGuessed> findAllByUserIdAndYearAndPosition(UUID userId, Year year, RacePosition position);

}
