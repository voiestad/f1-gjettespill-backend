package no.vebb.f1.notification.guessReminderPreference;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.notification.guessReminderOption.GuessReminderOption;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class GuessReminderPreferenceId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "guess_reminder_option", nullable = false)
    private GuessReminderOption option;

    protected GuessReminderPreferenceId() {
    }

    public GuessReminderPreferenceId(UUID userId, GuessReminderOption option) {
        this.userId = userId;
        this.option = option;
    }

    public GuessReminderOption option() {
        return option;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GuessReminderPreferenceId that)) return false;
        return Objects.equals(option, that.option) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, option);
    }
}
