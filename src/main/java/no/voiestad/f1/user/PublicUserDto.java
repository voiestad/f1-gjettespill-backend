package no.voiestad.f1.user;

import java.util.UUID;

public record PublicUserDto(UUID id, String username) {
	public static PublicUserDto fromEntity(UserEntity userEntity) {
		return new PublicUserDto(userEntity.id(), userEntity.username());
	}
}
