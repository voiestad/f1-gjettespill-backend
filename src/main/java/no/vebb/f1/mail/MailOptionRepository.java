package no.vebb.f1.mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailOptionRepository extends JpaRepository<MailOptionEntity, Integer> {
    List<MailOptionEntity> findAllByOrderByMailOption();
}
