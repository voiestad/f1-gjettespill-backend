package no.voiestad.f1.league.leagues;

import java.util.UUID;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "league_invitations")
public class LeagueInvitationEntity {
    @EmbeddedId
    private LeagueInvitationId id;

    protected LeagueInvitationEntity() {}

    public LeagueInvitationEntity(UUID userId, UUID leagueId, UUID inviter) {
        this.id = new LeagueInvitationId(userId, leagueId, inviter);
    }

    public UUID userId() {
        return id.userId();
    }

    public UUID leagueId() {
        return id.leagueId();
    }

    public UUID inviter() {
        return id.inviter();
    }
}
