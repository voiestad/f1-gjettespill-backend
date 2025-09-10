package no.vebb.f1.util.collection;

import no.vebb.f1.util.domainPrimitive.Flag;
import no.vebb.f1.stats.SessionType;

public record RegisteredFlag(Flag type, int round, int id, SessionType sessionType) {
}
