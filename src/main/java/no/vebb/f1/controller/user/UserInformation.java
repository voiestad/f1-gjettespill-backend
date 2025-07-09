package no.vebb.f1.controller.user;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.User;
import no.vebb.f1.util.collection.CompetitorGuessYear;
import no.vebb.f1.util.collection.FlagGuessYear;
import no.vebb.f1.util.collection.PlaceGuess;
import no.vebb.f1.util.collection.UserNotifiedCount;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;

public class UserInformation {

	public final User user;
	public final String email;
	public final List<CompetitorGuessYear<Driver>> driverGuess;
	public final List<CompetitorGuessYear<Constructor>> constructorGuess;
	public final List<FlagGuessYear> flagGuess;
	public final List<PlaceGuess> placeGuess;
	public final List<UserNotifiedCount> notifiedCount;
	public final List<Integer> emailPreferences;

	public UserInformation(User user, Database db) {
		this.user = user;
		this.email = db.getEmail(user.id);
		this.driverGuess = db.userGuessDataDriver(user.id);
		this.constructorGuess = db.userGuessDataConstructor(user.id);
		this.flagGuess = db.userGuessDataFlag(user.id);
		this.placeGuess = db.userGuessDataDriverPlace(user.id);
		this.notifiedCount = db.userDataNotified(user.id);
		this.emailPreferences = db.getMailingPreference(user.id).stream()
				.map(option -> option.value)
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					Collections.reverse(list);
					return list;
				}));
	}
}
