package no.vebb.f1.response;

import no.vebb.f1.notification.guessReminderOption.GuessReminderOption;

import java.util.Map;
import java.util.UUID;

public record GuessReminderOptionsResponse(UUID topic, Map<GuessReminderOption, Boolean> options) {
}
