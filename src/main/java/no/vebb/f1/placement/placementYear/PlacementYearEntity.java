package no.vebb.f1.placement.placementYear;

import jakarta.persistence.*;
import no.vebb.f1.placement.domain.UserPosition;
import no.vebb.f1.year.Year;

import java.util.UUID;

@Entity
@Table(name = "placements_year")
public class PlacementYearEntity {
    @EmbeddedId
    private PlacementYearId id;

    @Embedded
    private UserPosition placement;

    protected PlacementYearEntity() {}

    public PlacementYearEntity(Year year, UUID userId, UserPosition placement) {
        this.id = new PlacementYearId(year, userId);
        this.placement = placement;
    }

    public Year year() {
        return id.year();
    }

    public UUID userId() {
        return id.userId();
    }

    public UserPosition placement() {
        return placement;
    }

}
