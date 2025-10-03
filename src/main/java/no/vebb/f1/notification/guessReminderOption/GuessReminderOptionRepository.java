package no.vebb.f1.notification.guessReminderOption;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuessReminderOptionRepository extends JpaRepository<GuessReminderOptionEntity, GuessReminderOption> {
    List<GuessReminderOptionEntity> findAllByOrderByOption();
}
