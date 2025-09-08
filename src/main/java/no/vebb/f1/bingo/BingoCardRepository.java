package no.vebb.f1.bingo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BingoCardRepository extends JpaRepository<BingoCardEntity, BingoCardId> {
    boolean existsByIdYear(int year);
    List<BingoCardEntity> findAllByIdYearOrderByIdBingoSquareId(int year);

    @Query("""
        UPDATE BingoCardEntity
        SET squareText = :text
        WHERE id.year = :year AND id.bingoSquareId = :id
    """)
    @Modifying
    void updateText(int year, int id, String text);
}
