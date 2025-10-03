package no.vebb.f1.notification.guessReminderPreference;

import jakarta.persistence.*;
import no.vebb.f1.notification.guessReminderOption.GuessReminderOption;


@Entity
@Table(name = "guess_reminder_preferences")
public class GuessReminderPreferenceEntity {
    @EmbeddedId
    private GuessReminderPreferenceId id;

    protected GuessReminderPreferenceEntity() {}

    public GuessReminderPreferenceEntity(GuessReminderPreferenceId id) {
        this.id = id;
    }

    public GuessReminderOption option() {
        return id.option();
    }
}
