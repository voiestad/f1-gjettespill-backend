package no.vebb.f1.user;

import java.util.UUID;

public record UserDto(UUID id, String username, String googleId) {
    public static UserDto fromEntity(User user) {
        return new UserDto(user.id(), user.username(), user.googleId());
    }
}
