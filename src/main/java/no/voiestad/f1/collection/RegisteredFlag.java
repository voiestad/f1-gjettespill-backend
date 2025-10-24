package no.voiestad.f1.collection;

import no.voiestad.f1.stats.domain.Flag;
import no.voiestad.f1.stats.domain.SessionType;

public record RegisteredFlag(Flag type, int round, int id, SessionType sessionType) {
}
