package no.voiestad.f1.bingo;

import no.voiestad.f1.year.Year;

public record BingoSquare(String text, boolean marked, int id, Year year) {
    public static BingoSquare fromBingoCardEntity(BingoCardEntity bingoCardEntity) {
        return new BingoSquare(bingoCardEntity.squareText(), bingoCardEntity.isMarked(),
                bingoCardEntity.bingoSquareId(), bingoCardEntity.year());
    }

}
