package no.vebb.f1.util;

import java.time.LocalDateTime;

public 	class CutoffRace {
	public final int position;
	public final String name;
	public final int id;
	public final LocalDateTime cutoff;

	public CutoffRace(int position, String name, int id) {
		this(position, name, id, null);
	}

	public CutoffRace(int position, String name, int id, LocalDateTime cutoff) {
		this.position = position;
		this.name = name;
		this.id = id;
		this.cutoff = cutoff;
	}
}
