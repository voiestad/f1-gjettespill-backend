package no.voiestad.f1.placement.leaguePlacementRace;

import java.util.Objects;
import java.util.UUID;

import no.voiestad.f1.year.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class LeaguePlacementRaceYearStartId {
    @Embedded
    private Year year;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    protected LeaguePlacementRaceYearStartId() {}

    public LeaguePlacementRaceYearStartId(Year year, UUID userId, UUID leagueId) {
        this.year = year;
        this.userId = userId;
        this.leagueId = leagueId;
    }

    public Year year() {
        return year;
    }

    public UUID userId() {
        return userId;
    }

    public UUID leagueId() {
        return leagueId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LeaguePlacementRaceYearStartId that = (LeaguePlacementRaceYearStartId) o;
        return Objects.equals(year, that.year) && Objects.equals(userId, that.userId) && Objects.equals(leagueId, that.leagueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, userId, leagueId);
    }
}
