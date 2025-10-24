package no.voiestad.f1.codes;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralCodeRepository extends JpaRepository<ReferralCodeEntity, UUID> {
    Optional<ReferralCodeEntity> findByCode(long code);
}
