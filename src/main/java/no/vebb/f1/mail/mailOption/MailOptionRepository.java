package no.vebb.f1.mail.mailOption;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailOptionRepository extends JpaRepository<MailOptionEntity, MailOption> {
    List<MailOptionEntity> findAllByOrderByMailOption();
}
