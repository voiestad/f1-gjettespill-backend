package no.vebb.f1.bingo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class BingoCardId {
    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "bingo_square_id", nullable = false)
    private int bingoSquareId;

    protected BingoCardId() {}

    public BingoCardId(int year, int bingoSquareId) {
        this.year = year;
        this.bingoSquareId = bingoSquareId;
    }

    public int year() {
        return year;
    }

    public int bingoSquareId() {
        return bingoSquareId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BingoCardId that)) return false;
        return year == that.year && bingoSquareId == that.bingoSquareId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, bingoSquareId);
    }
}
