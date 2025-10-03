package no.vebb.f1.scoring.diffPointsMap;

import jakarta.persistence.*;
import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.year.Year;

import java.util.Objects;

@Embeddable
public class DiffPointsMapId {
    @Column(name = "category_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category categoryName;
    @Embedded
    private Diff diff;
    @Embedded
    private Year year;

    protected DiffPointsMapId() {}

    public DiffPointsMapId(Category categoryName, Diff diff, Year year) {
        this.categoryName = categoryName;
        this.diff = diff;
        this.year = year;
    }

    public Category categoryName() {
        return categoryName;
    }

    public Diff diff() {
        return diff;
    }

    public Year year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DiffPointsMapId that)) return false;
        return Objects.equals(diff, that.diff) && Objects.equals(year, that.year) && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, diff, year);
    }
}
