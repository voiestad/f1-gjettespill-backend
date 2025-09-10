package no.vebb.f1.results.raceResult;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import no.vebb.f1.race.RaceId;
import no.vebb.f1.results.domain.CompetitorPosition;

import java.util.Objects;

@Embeddable
public class RaceResultId {
    @Embedded
    private RaceId raceId;
    @Embedded
    private CompetitorPosition finishingPosition;

    protected RaceResultId() {}

    public RaceResultId(RaceId raceId, CompetitorPosition finishingPosition) {
        this.raceId = raceId;
        this.finishingPosition = finishingPosition;
    }
    public RaceId raceId() {
        return raceId;
    }

    public CompetitorPosition finishingPosition() {
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
