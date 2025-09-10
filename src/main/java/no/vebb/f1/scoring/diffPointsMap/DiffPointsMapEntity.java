package no.vebb.f1.scoring.diffPointsMap;

import jakarta.persistence.*;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.year.Year;

@Entity
@Table(name = "diff_points_mappings")
public class DiffPointsMapEntity {
    @EmbeddedId
    private DiffPointsMapId id;

    @Embedded
    private UserPoints points;

    protected DiffPointsMapEntity() {}

    public DiffPointsMapEntity(Category categoryName, Diff diff, Year year, UserPoints points) {
        this.id = new DiffPointsMapId(categoryName, diff, year);
        this.points = points;
    }

    public Category categoryName() {
        return id.categoryName();
    }

    public Diff diff() {
        return id.diff();
    }

    public Year year() {
        return id.year();
    }

    public UserPoints points() {
        return points;
    }
}
