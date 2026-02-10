package no.voiestad.f1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserLinkingRepository extends JpaRepository<UserLinkingEntity, Integer> {
    @Query(value = "SELECT NEXTVAL('user_providers_id_seq')", nativeQuery = true)
    int getNextId();
    Optional<UserLinkingEntity> findByCode(String code);
    @Modifying
    void deleteByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM UserLinkingEntity WHERE validTo < :time")
    void deleteExpiredCodes(Instant time);
}
