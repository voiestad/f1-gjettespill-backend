package no.vebb.f1.scoring;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "diff_points_mappings")
public class DiffPointsMapEntity {
    @EmbeddedId
    private DiffPointsMapId id;

    @Column(name = "points", nullable = false)
    private int points;

    protected DiffPointsMapEntity() {}

    public DiffPointsMapEntity(String categoryName, int diff, int year, int points) {
        this.id = new DiffPointsMapId(categoryName, diff, year);
        this.points = points;
    }

    public String categoryName() {
        return id.categoryName();
    }

    public int diff() {
        return id.diff();
    }

    public int year() {
        return id.year();
    }

    public int points() {
        return points;
    }
}
