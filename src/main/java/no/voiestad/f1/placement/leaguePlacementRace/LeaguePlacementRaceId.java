package no.voiestad.f1.placement.leaguePlacementRace;

import java.util.Objects;
import java.util.UUID;

import no.voiestad.f1.race.RaceId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class LeaguePlacementRaceId {
    @Embedded
    private RaceId raceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    protected LeaguePlacementRaceId() {}

    public LeaguePlacementRaceId(RaceId raceId, UUID userId, UUID leagueId) {
        this.raceId = raceId;
        this.userId = userId;
        this.leagueId = leagueId;
    }

    public RaceId raceId() {
        return raceId;
    }

    public UUID userId() {
        return userId;
    }

    public UUID leagueId() {
        return leagueId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeaguePlacementRaceId that)) return false;
        return Objects.equals(raceId, that.raceId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raceId, userId);
    }
}
