package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Color;

public class ColoredCompetitor<T> {
	
	public final T competitor;
	public final Color color;

	public ColoredCompetitor(T competitor, Color color) {
		this.competitor = competitor;
		this.color = color;
	}
}
