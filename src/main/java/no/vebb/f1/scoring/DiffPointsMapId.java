package no.vebb.f1.scoring;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DiffPointsMapId {
    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "diff", nullable = false)
    private int diff;

    @Column(name = "year", nullable = false)
    private int year;

    protected DiffPointsMapId() {}

    public DiffPointsMapId(String categoryName, int diff, int year) {
        this.categoryName = categoryName;
        this.diff = diff;
        this.year = year;
    }

    public String categoryName() {
        return categoryName;
    }

    public int diff() {
        return diff;
    }

    public int year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DiffPointsMapId that)) return false;
        return diff == that.diff && year == that.year && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, diff, year);
    }
}
