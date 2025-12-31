package no.voiestad.f1.league.leagues;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LeagueInvitationId {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    @Column(name = "inviter", nullable = false)
    private UUID inviter;

    protected LeagueInvitationId() {}

    public LeagueInvitationId(UUID userId, UUID leagueId, UUID inviter) {
        this.userId = userId;
        this.leagueId = leagueId;
        this.inviter = inviter;
    }

    public UUID userId() {
        return userId;
    }

    public UUID leagueId() {
        return leagueId;
    }

    public UUID inviter() {
        return inviter;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeagueInvitationId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(leagueId, that.leagueId) && Objects.equals(inviter, that.inviter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, leagueId, inviter);
    }
}
