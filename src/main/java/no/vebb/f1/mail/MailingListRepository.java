package no.vebb.f1.mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MailingListRepository extends JpaRepository<MailEntity, UUID> {
}
