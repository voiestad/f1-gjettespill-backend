package no.voiestad.f1.notification.guessReminderPreference;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface GuessReminderPreferenceRepository extends JpaRepository<GuessReminderPreferenceEntity, GuessReminderPreferenceId> {

    @Modifying
    void deleteByIdUserId(UUID userId);

    List<GuessReminderPreferenceEntity> findAllByIdUserIdOrderByIdOption(UUID userId);
}
