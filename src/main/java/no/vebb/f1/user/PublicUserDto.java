package no.vebb.f1.user;

import java.util.UUID;

public record PublicUserDto(UUID id, String username) {
	public static PublicUserDto fromEntity(User user) {
		return new PublicUserDto(user.id(), user.username());
	}
}
