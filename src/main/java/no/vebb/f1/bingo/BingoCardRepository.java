package no.vebb.f1.bingo;

import no.vebb.f1.year.Year;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BingoCardRepository extends JpaRepository<BingoCardEntity, BingoCardId> {
    boolean existsByIdYear(Year year);
    List<BingoCardEntity> findAllByIdYearOrderByIdBingoSquareId(Year year);
}
