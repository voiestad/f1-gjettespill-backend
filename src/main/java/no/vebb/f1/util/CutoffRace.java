package no.vebb.f1.util;

import java.time.LocalDateTime;

public 	class CutoffRace {
	public final int position;
	public final String name;
	public final RaceId id;
	public final LocalDateTime cutoff;
	public final Year year;

	public CutoffRace(int position, String name, RaceId id) {
		this(position, name, id, null, null);
	}

	public CutoffRace(int position, String name, RaceId id, LocalDateTime cutoff, Year year) {
		this.position = position;
		this.name = name;
		this.id = id;
		this.cutoff = cutoff;
		this.year = year;
	}

	public CutoffRace(int position, RaceId id, Year year) {
		this(position, null, id, null, year);
	}
}
