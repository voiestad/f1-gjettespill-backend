package no.voiestad.f1.bingo;

import java.util.Objects;

import no.voiestad.f1.year.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class BingoCardId {
    @Embedded
    private Year year;

    @Column(name = "bingo_square_id", nullable = false)
    private int bingoSquareId;

    protected BingoCardId() {}

    public BingoCardId(Year year, int bingoSquareId) {
        this.year = year;
        this.bingoSquareId = bingoSquareId;
    }

    public Year year() {
        return year;
    }

    public int bingoSquareId() {
        return bingoSquareId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BingoCardId that)) return false;
        return Objects.equals(year, that.year) && bingoSquareId == that.bingoSquareId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, bingoSquareId);
    }
}
