package no.voiestad.f1.collection;

import no.voiestad.f1.placement.domain.UserPosition;

public record Placement<T>(UserPosition pos, T value) {
}
