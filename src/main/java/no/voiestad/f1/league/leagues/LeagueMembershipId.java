package no.voiestad.f1.league.leagues;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LeagueMembershipId {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    protected LeagueMembershipId() {}

    public LeagueMembershipId(UUID userId, UUID leagueId) {
        this.userId = userId;
        this.leagueId = leagueId;
    }

    public UUID leagueId() {
        return leagueId;
    }

    public UUID userId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeagueMembershipId that)) return false;
        return Objects.equals(leagueId, that.leagueId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leagueId, userId);
    }
}
