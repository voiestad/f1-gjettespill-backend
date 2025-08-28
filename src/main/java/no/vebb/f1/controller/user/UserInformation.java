package no.vebb.f1.controller.user;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.vebb.f1.database.Database;
import no.vebb.f1.mail.MailService;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserDto;
import no.vebb.f1.util.collection.CompetitorGuessYear;
import no.vebb.f1.util.collection.FlagGuessYear;
import no.vebb.f1.util.collection.PlaceGuess;
import no.vebb.f1.util.collection.UserNotifiedCount;
import no.vebb.f1.util.domainPrimitive.Constructor;
import no.vebb.f1.util.domainPrimitive.Driver;

public class UserInformation {

	public final UserDto user;
	public final String email;
	public final List<CompetitorGuessYear<Driver>> driverGuess;
	public final List<CompetitorGuessYear<Constructor>> constructorGuess;
	public final List<FlagGuessYear> flagGuess;
	public final List<PlaceGuess> placeGuess;
	public final List<UserNotifiedCount> notifiedCount;
	public final List<Integer> emailPreferences;

	public UserInformation(UserEntity userEntity, Database db, MailService mailService) {
		this.user = UserDto.fromEntity(userEntity);
		this.email = mailService.getEmail(userEntity.id());
		this.driverGuess = db.userGuessDataDriver(userEntity.id());
		this.constructorGuess = db.userGuessDataConstructor(userEntity.id());
		this.flagGuess = db.userGuessDataFlag(userEntity.id());
		this.placeGuess = db.userGuessDataDriverPlace(userEntity.id());
		this.notifiedCount = db.userDataNotified(userEntity.id());
		this.emailPreferences = mailService.getMailingPreference(userEntity.id()).stream()
				.map(option -> option.value)
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					Collections.reverse(list);
					return list;
				}));
	}
}
