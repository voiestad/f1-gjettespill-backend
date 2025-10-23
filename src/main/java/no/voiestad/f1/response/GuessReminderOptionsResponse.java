package no.voiestad.f1.response;

import java.util.Map;
import java.util.UUID;

import no.voiestad.f1.notification.guessReminderOption.GuessReminderOption;

public record GuessReminderOptionsResponse(UUID topic, Map<GuessReminderOption, Boolean> options) {
}
