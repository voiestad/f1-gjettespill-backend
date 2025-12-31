package no.voiestad.f1.league.leagues;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "league_memberships")
public class LeagueMembershipEntity {
    @EmbeddedId
    private LeagueMembershipId id;

    protected LeagueMembershipEntity() {
    }

    public LeagueMembershipEntity(UUID userId, UUID leagueId) {
        this.id = new LeagueMembershipId(userId, leagueId);
    }

    public UUID leagueId() {
        return id.leagueId();
    }

    public UUID userId() {
        return id.userId();
    }
}
