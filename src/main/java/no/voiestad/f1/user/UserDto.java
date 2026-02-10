package no.voiestad.f1.user;

import java.util.Map;
import java.util.UUID;

public record UserDto(UUID id, String username, Map<String, String> providers) {
    public static UserDto fromEntity(UserEntity userEntity, Map<String, String> providers) {
        return new UserDto(userEntity.id(), userEntity.username(), providers);
    }
}
