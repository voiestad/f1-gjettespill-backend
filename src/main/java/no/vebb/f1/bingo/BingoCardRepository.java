package no.vebb.f1.bingo;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BingoCardRepository extends JpaRepository<BingoCardEntity, BingoCardId> {
    boolean existsByIdYear(Year year);
    List<BingoCardEntity> findAllByIdYearOrderByIdBingoSquareId(Year year);

    @Query("""
        UPDATE BingoCardEntity
        SET squareText = :text
        WHERE id.year = :year AND id.bingoSquareId = :id
    """)
    @Modifying
    void updateText(Year year, int id, String text);
}
