package no.voiestad.f1.placement.leaguePlacementRace;

import java.util.UUID;

import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.race.RaceId;

import jakarta.persistence.*;

@Entity
@Table(name = "league_placements_race")
public class LeaguePlacementRaceEntity {
    @EmbeddedId
    private LeaguePlacementRaceId id;

    @Embedded
    private UserPosition placement;

    protected LeaguePlacementRaceEntity() {}

    public LeaguePlacementRaceEntity(RaceId raceId, UUID userId, UUID leagueId, UserPosition placement) {
        this.id = new LeaguePlacementRaceId(raceId, userId, leagueId);
        this.placement = placement;
    }

    public RaceId raceId() {
        return id.raceId();
    }

    public UUID userId() {
        return id.userId();
    }

    public UUID leagueId() {
        return id.leagueId();
    }

    public UserPosition placement() {
        return placement;
    }

}
