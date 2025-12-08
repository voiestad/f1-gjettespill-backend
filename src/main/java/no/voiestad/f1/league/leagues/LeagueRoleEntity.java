package no.voiestad.f1.league.leagues;

import java.util.UUID;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.voiestad.f1.league.domain.LeagueRole;

@Entity
@Table(name = "league_roles")
public class LeagueRoleEntity {
    @EmbeddedId
    private LeagueRoleId id;

    protected LeagueRoleEntity() {
    }

    public LeagueRoleEntity(UUID userId, UUID leagueId, LeagueRole leagueRole) {
        this.id = new LeagueRoleId(userId, leagueId, leagueRole);
    }

    public UUID userId() {
        return id.userId();
    }

    public UUID leagueId() {
        return id.leagueId();
    }

    public LeagueRole leagueRole() {
        return id.leagueRole();
    }
}
