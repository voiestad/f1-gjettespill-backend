package no.vebb.f1.util.collection;

public class ValuedCompetitor<T, U> {
	public final T competitor;
	public final U value;

	public ValuedCompetitor(T competitor, U value) {
		this.competitor = competitor;
		this.value = value;
	}
}
