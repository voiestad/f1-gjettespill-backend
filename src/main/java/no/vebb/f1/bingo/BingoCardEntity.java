package no.vebb.f1.bingo;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.vebb.f1.util.domainPrimitive.Year;

@Entity
@Table(name = "bingo_cards")
public class BingoCardEntity {
    @EmbeddedId
    private BingoCardId id;

    @Column(name = "square_text")
    private String squareText;

    @Column(name = "marked")
    private boolean marked;

    protected BingoCardEntity() {}

    public BingoCardEntity(Year year, int bingoSquareId, String squareText, boolean marked) {
        this.id = new BingoCardId(year, bingoSquareId);
        this.squareText = squareText;
        this.marked = marked;
    }

    public Year year() {
        return id.year();
    }

    public int bingoSquareId() {
        return id.bingoSquareId();
    }

    public String squareText() {
        return squareText;
    }

    public boolean isMarked() {
        return marked;
    }

    public BingoCardEntity toggled() {
        return new BingoCardEntity(id.year(), id.bingoSquareId(), squareText, !marked);
    }
}
