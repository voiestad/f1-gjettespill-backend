package no.vebb.f1.placement;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "placements_year")
public class PlacementYearEntity {
    @EmbeddedId
    private PlacementYearId id;

    @Column(name = "placement")
    private int placement;

    protected PlacementYearEntity() {}

    public PlacementYearEntity(int year, UUID userId, int placement) {
        this.id = new PlacementYearId(year, userId);
        this.placement = placement;
    }

    public int year() {
        return id.year();
    }

    public UUID userId() {
        return id.userId();
    }

    public int placement() {
        return placement;
    }

}
