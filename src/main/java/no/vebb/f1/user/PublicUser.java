package no.vebb.f1.user;

import java.util.UUID;

public class PublicUser {
	
	public final UUID id;
	public final String username;

	public PublicUser(User user) {
		this.id = user.id;
		this.username = user.username;
	}
}
