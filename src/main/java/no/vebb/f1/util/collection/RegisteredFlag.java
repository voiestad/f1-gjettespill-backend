package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Flag;

public class RegisteredFlag {
	public final Flag type;
	public final int round;
	public final int id;

	public RegisteredFlag(Flag type, int round, int id) {
		this.type = type;
		this.round = round;
		this.id = id;
	}

}
