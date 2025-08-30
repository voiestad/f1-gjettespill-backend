package no.vebb.f1.mail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface MailPreferenceRepository extends JpaRepository<MailPreferenceEntity, MailPreferenceId> {

    @Modifying
    void deleteByIdUserId(UUID userId);

    List<MailPreferenceEntity> findAllByIdUserIdOrderByIdMailOption(UUID userId);
}
