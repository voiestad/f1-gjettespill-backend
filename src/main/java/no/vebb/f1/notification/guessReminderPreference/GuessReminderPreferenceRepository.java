package no.vebb.f1.notification.guessReminderPreference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface GuessReminderPreferenceRepository extends JpaRepository<GuessReminderPreferenceEntity, GuessReminderPreferenceId> {

    @Modifying
    void deleteByIdUserId(UUID userId);

    List<GuessReminderPreferenceEntity> findAllByIdUserIdOrderByIdOption(UUID userId);
}
