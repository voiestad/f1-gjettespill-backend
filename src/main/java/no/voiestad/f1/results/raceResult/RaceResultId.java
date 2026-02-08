package no.voiestad.f1.results.raceResult;
import java.util.Objects;

import no.voiestad.f1.race.RaceId;
import no.voiestad.f1.results.domain.CompetitorPosition;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class RaceResultId {
    @Embedded
    private RaceId raceId;
    @Embedded
    private CompetitorPosition position;

    protected RaceResultId() {}

    public RaceResultId(RaceId raceId, CompetitorPosition position) {
        this.raceId = raceId;
        this.position = position;
    }
    public RaceId raceId() {
        return raceId;
    }

    public CompetitorPosition position() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RaceResultId that)) return false;
        return Objects.equals(raceId, that.raceId) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, position);
    }
}
