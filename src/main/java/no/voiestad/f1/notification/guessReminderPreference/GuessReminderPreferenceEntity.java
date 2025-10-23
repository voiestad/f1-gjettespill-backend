package no.voiestad.f1.notification.guessReminderPreference;

import no.voiestad.f1.notification.guessReminderOption.GuessReminderOption;

import jakarta.persistence.*;

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
