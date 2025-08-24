package no.vebb.f1.codes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReferralCodeRepository extends JpaRepository<ReferralCode, UUID> {
    Optional<ReferralCode> findByCode(long code);
}
