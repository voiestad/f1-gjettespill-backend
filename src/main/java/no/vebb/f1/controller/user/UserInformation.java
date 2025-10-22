package no.vebb.f1.controller.user;

import java.util.List;
import java.util.UUID;

import no.vebb.f1.collection.CompetitorGuessYear;
import no.vebb.f1.collection.FlagGuessYear;
import no.vebb.f1.competitors.domain.ConstructorName;
import no.vebb.f1.competitors.domain.DriverName;
import no.vebb.f1.guessing.collection.PlaceGuess;
import no.vebb.f1.collection.UserNotifiedCount;
import no.vebb.f1.guessing.GuessService;
import no.vebb.f1.notification.NotificationService;
import no.vebb.f1.notification.guessReminderOption.GuessReminderOption;
import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserDto;

public class UserInformation {

	public final UserDto user;
	public final UUID topic;
	public final List<CompetitorGuessYear<DriverName>> driverGuess;
	public final List<CompetitorGuessYear<ConstructorName>> constructorGuess;
	public final List<FlagGuessYear> flagGuess;
	public final List<PlaceGuess> placeGuess;
	public final List<UserNotifiedCount> notifiedCount;
	public final List<GuessReminderOption> guessReminderPreferences;

	public UserInformation(UserEntity userEntity, NotificationService notificationService, GuessService guessService) {
		this.user = UserDto.fromEntity(userEntity);
		this.topic = notificationService.getNtfyTopic(userEntity.id()).orElse(null);
		this.driverGuess = guessService.userGuessDataDriver(userEntity.id());
		this.constructorGuess = guessService.userGuessDataConstructor(userEntity.id());
		this.flagGuess = guessService.userGuessDataFlag(userEntity.id());
		this.placeGuess = guessService.userGuessDataDriverPlace(userEntity.id());
		this.notifiedCount = notificationService.userDataNotified(userEntity.id());
		this.guessReminderPreferences = notificationService.getGuessReminderPreference(userEntity.id());
	}
}
