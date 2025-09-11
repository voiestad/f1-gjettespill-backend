package no.vebb.f1.results.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.scoring.domain.Diff;

import java.util.Optional;

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
