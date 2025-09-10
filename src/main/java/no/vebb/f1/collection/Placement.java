package no.vebb.f1.collection;

import no.vebb.f1.placement.domain.UserPosition;

public record Placement<T>(UserPosition pos, T value) {
}
