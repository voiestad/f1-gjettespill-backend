package no.vebb.f1.user;

import java.util.UUID;

public class User {

	public final String googleId;
	public final UUID id;
	public final String username;

	public User(String googleId, UUID id, String username) {
		this.googleId = googleId;
		this.id = id;
		this.username = username;		
	}

	public String getUsername() {
		return username;
	}

	public String getGoogleId() {
		return googleId;
	}

	public UUID getId() {
		return id;
	}
	
}
