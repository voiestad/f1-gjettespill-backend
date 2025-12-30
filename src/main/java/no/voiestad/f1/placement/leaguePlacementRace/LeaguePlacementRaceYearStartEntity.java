package no.voiestad.f1.placement.leaguePlacementRace;

import java.util.UUID;

import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

@Entity
@Table(name = "league_placements_race_year_start")
public class LeaguePlacementRaceYearStartEntity {
    @EmbeddedId
    private LeaguePlacementRaceYearStartId id;

    @Embedded
    private UserPosition placement;

    protected LeaguePlacementRaceYearStartEntity() {}

    public LeaguePlacementRaceYearStartEntity(Year year, UUID userId, UUID leagueId, UserPosition placement) {
        this.id = new LeaguePlacementRaceYearStartId(year, userId, leagueId);
        this.placement = placement;
    }

    public Year year() {
        return id.year();
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
