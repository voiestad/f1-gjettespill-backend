package no.voiestad.f1.placement.placementYear;

import java.util.UUID;

import no.voiestad.f1.placement.domain.UserPosition;
import no.voiestad.f1.year.Year;

import jakarta.persistence.*;

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
