package no.vebb.f1.user;

public class User {

	public final String id;
	public final String username;

	public User(String id, String username) {
		this.id = id;
		this.username = username;		
	}

	public String getUsername() {
		return username;
	}

	public String getId() {
		return id;
	}
	
}
