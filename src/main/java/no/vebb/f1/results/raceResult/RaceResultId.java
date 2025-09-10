package no.vebb.f1.results.raceResult;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.race.RaceId;

import java.util.Objects;

@Embeddable
public class RaceResultId {
    @Embedded
    private RaceId raceId;
    @Column(name = "finishing_position", nullable = false)
    private int finishingPosition;

    protected RaceResultId() {}

    public RaceResultId(RaceId raceId, int finishingPosition) {
        this.raceId = raceId;
        this.finishingPosition = finishingPosition;
    }
    public RaceId raceId() {
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
