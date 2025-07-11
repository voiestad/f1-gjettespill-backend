package no.vebb.f1.user;

import java.util.UUID;

public record User(String googleId, UUID id, String username) {
}
