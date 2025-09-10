package no.vebb.f1.collection;

import no.vebb.f1.stats.domain.Flag;
import no.vebb.f1.stats.domain.SessionType;

public record RegisteredFlag(Flag type, int round, int id, SessionType sessionType) {
}
