package no.vebb.f1.notification.guessReminderOption;

import jakarta.persistence.*;

@Entity
@Table(name = "guess_reminder_options")
public class GuessReminderOptionEntity {
    @EmbeddedId
    private GuessReminderOption option;

    public GuessReminderOption option() {
        return option;
    }
}
