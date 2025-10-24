package no.voiestad.f1.bingo;

import java.util.List;

import no.voiestad.f1.year.Year;

import org.springframework.data.jpa.repository.JpaRepository;


public interface BingoCardRepository extends JpaRepository<BingoCardEntity, BingoCardId> {
    boolean existsByIdYear(Year year);
    List<BingoCardEntity> findAllByIdYearOrderByIdBingoSquareId(Year year);
}
