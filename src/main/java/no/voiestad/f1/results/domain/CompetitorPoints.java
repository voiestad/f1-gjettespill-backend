package no.voiestad.f1.results.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import no.voiestad.f1.scoring.domain.Diff;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import com.fasterxml.jackson.annotation.JsonValue;


@Embeddable
public class CompetitorPoints implements Comparable<CompetitorPoints> {
    @Column(name = "points", nullable = false)
    public final int value;

    private CompetitorPoints(int value) {
        this.value = value;
    }

    public CompetitorPoints() {
        this.value = 0;
    }

    public static Optional<CompetitorPoints> getCompetitorPoints(int value) {
        CompetitorPoints competitorPoints = new CompetitorPoints(value);
        if (competitorPoints.isValid()) {
            return Optional.of(competitorPoints);
        }
        return Optional.empty();
    }

    public static CompetitorPoints fromDiff(Diff diff) {
        return new CompetitorPoints(diff.toValue());
    }

    private boolean isValid() {
        return value >= 0;
    }

    public CompetitorPoints add(CompetitorPoints other) {
        return new CompetitorPoints(this.value + other.value);
    }

    public static <T> Optional<List<CompetitorPoints>> extractCompetitorPoints(
            List<T> values,
            Function<T, Integer> extractor) {
        int expectedLength = values.size();
        List<CompetitorPoints> points = values.stream()
                .map(extractor)
                .map(CompetitorPoints::getCompetitorPoints)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        if (points.size() != expectedLength) {
            return Optional.empty();
        }
        return Optional.of(points);
    }

    @JsonValue
    public int toValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompetitorPoints other = (CompetitorPoints) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int compareTo(CompetitorPoints o) {
        return Integer.compare(this.value, o.value);
    }
}
