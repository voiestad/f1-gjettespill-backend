package no.vebb.f1.controller.user;

import java.util.List;

import no.vebb.f1.collection.CompetitorGuessYear;
import no.vebb.f1.collection.FlagGuessYear;
import no.vebb.f1.guessing.collection.PlaceGuess;
import no.vebb.f1.collection.UserNotifiedCount;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.mail.domain.Email;
import no.vebb.f1.mail.MailService;
import no.vebb.f1.mail.mailOption.MailOption;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserDto;
import no.vebb.f1.competitors.domain.Constructor;
import no.vebb.f1.competitors.domain.Driver;

public class UserInformation {

	public final UserDto user;
	public final Email email;
	public final List<CompetitorGuessYear<Driver>> driverGuess;
	public final List<CompetitorGuessYear<Constructor>> constructorGuess;
	public final List<FlagGuessYear> flagGuess;
	public final List<PlaceGuess> placeGuess;
	public final List<UserNotifiedCount> notifiedCount;
	public final List<MailOption> emailPreferences;

	public UserInformation(UserEntity userEntity, MailService mailService, GuessService guessService) {
		this.user = UserDto.fromEntity(userEntity);
		this.email = mailService.getEmail(userEntity.id());
		this.driverGuess = guessService.userGuessDataDriver(userEntity.id());
		this.constructorGuess = guessService.userGuessDataConstructor(userEntity.id());
		this.flagGuess = guessService.userGuessDataFlag(userEntity.id());
		this.placeGuess = guessService.userGuessDataDriverPlace(userEntity.id());
		this.notifiedCount = mailService.userDataNotified(userEntity.id());
		this.emailPreferences = mailService.getMailingPreference(userEntity.id());
	}
}
