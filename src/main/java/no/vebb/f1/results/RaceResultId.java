package no.vebb.f1.results;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class RaceResultId {
    @Column(name = "race_id", nullable = false)
    private int raceId;
    @Column(name = "finishing_position", nullable = false)
    private int finishingPosition;

    protected RaceResultId() {}

    public RaceResultId(int raceId, int finishingPosition) {
        this.raceId = raceId;
        this.finishingPosition = finishingPosition;
    }
    public int raceId() {
        return raceId;
    }

    public int finishingPosition() {
        return finishingPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RaceResultId that)) return false;
        return raceId == that.raceId && finishingPosition == that.finishingPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, finishingPosition);
    }
}
