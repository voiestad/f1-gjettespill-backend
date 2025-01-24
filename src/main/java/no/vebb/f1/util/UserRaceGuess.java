package no.vebb.f1.util;

public class UserRaceGuess {
	
	public final String user;
	public final String driver;
	public final String position;

	public UserRaceGuess(String user, String driver, int position) {
		this.user = user;
		this.driver = driver;
		this.position = Integer.toString(position);
	}
}
