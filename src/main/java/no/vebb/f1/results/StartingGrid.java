package no.vebb.f1.results;

import jakarta.persistence.*;

@Entity
@Table(name = "starting_grids")
public class StartingGrid {
    @EmbeddedId
    private StartingGridId id;

    @Column(name = "position", nullable = false)
    private int position;

    protected StartingGrid() {
    }

    public StartingGrid(int raceId, String driverName, int position) {
        this.id = new StartingGridId(raceId, driverName);
        this.position = position;
    }

    public int raceId() {
        return id.raceId();
    }

    public String driverName() {
        return id.driverName();
    }

    public int position() {
        return position;
    }
}
