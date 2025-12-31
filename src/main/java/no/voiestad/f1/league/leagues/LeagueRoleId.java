package no.voiestad.f1.league.leagues;

import java.util.Objects;
import java.util.UUID;

import no.voiestad.f1.league.domain.LeagueRole;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class LeagueRoleId {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    @Column(name = "league_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeagueRole leagueRole;

    protected LeagueRoleId() {}

    public LeagueRoleId(UUID userId, UUID leagueId, LeagueRole leagueRole) {
        this.userId = userId;
        this.leagueId = leagueId;
        this.leagueRole = leagueRole;
    }

    public UUID leagueId() {
        return leagueId;
    }

    public UUID userId() {
        return userId;
    }

    public LeagueRole leagueRole() {
        return leagueRole;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeagueRoleId that)) return false;
        return Objects.equals(leagueId, that.leagueId) && Objects.equals(userId, that.userId) && leagueRole == that.leagueRole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(leagueId, userId, leagueRole);
    }
}
