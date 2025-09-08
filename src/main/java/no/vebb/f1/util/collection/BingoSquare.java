package no.vebb.f1.util.collection;

import no.vebb.f1.bingo.BingoCardEntity;
import no.vebb.f1.util.domainPrimitive.Year;

public record BingoSquare(String text, boolean marked, int id, Year year) {
    public static BingoSquare fromBingoCardEntity(BingoCardEntity bingoCardEntity) {
        return new BingoSquare(bingoCardEntity.squareText(), bingoCardEntity.isMarked(),
                bingoCardEntity.bingoSquareId(), new Year(bingoCardEntity.year()));
    }

}
