package no.vebb.f1.mail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MailPreferenceRepository extends JpaRepository<MailPreference, MailPreferenceId> {

    @Modifying
    @Query(value = "DELETE FROM mail_preferences WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(UUID userId);

    @Query(value = "SELECT * FROM mail_preferences WHERE user_id = :userId ORDER BY mail_option DESC",
            nativeQuery = true)
    List<MailPreference> findAllByUserId(UUID userId);
}
