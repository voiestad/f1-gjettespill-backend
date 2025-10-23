package no.voiestad.f1.notification.guessReminderOption;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuessReminderOptionRepository extends JpaRepository<GuessReminderOptionEntity, GuessReminderOption> {
    List<GuessReminderOptionEntity> findAllByOrderByOption();
}
