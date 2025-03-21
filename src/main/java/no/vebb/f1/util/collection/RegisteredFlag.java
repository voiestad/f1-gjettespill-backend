package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.util.domainPrimitive.SessionType;

public class RegisteredFlag {
	public final Flag type;
	public final int round;
	public final int id;
	public final SessionType sessionType;

	public RegisteredFlag(Flag type, int round, int id, SessionType sessionType) {
		this.type = type;
		this.round = round;
		this.id = id;
		this.sessionType = sessionType;
	}

}
