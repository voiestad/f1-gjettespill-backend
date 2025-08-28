package no.vebb.f1.user;

import java.util.UUID;

public record UserDto(UUID id, String username, String googleId) {
    public static UserDto fromEntity(UserEntity userEntity) {
        return new UserDto(userEntity.id(), userEntity.username(), userEntity.googleId());
    }
}
