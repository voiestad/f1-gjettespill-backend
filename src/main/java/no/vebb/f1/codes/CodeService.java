package no.vebb.f1.codes;

import no.vebb.f1.util.TimeUtil;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class CodeService {

    private final ReferralCodeRepository referralCodeRepository;

    public CodeService(ReferralCodeRepository referralCodeRepository) {
        this.referralCodeRepository = referralCodeRepository;
    }

    private boolean isExpired(Code code) {
        return code.cutoff().compareTo(Instant.now()) < 0;
    }

    private boolean isValid(Code code) {
        return !isExpired(code);
    }

    public boolean isValidReferralCode(long code) {
        return referralCodeRepository.findByCode(code).map(this::isValid).orElse(false);
    }

    public void removeExpiredReferralCodes() {
        referralCodeRepository.deleteAll(
                referralCodeRepository.findAll().stream().filter(this::isExpired).toList()
        );
    }

    public long addReferralCode(UUID userId) {
        long code = CodeGenerator.getReferralCode();
        Instant cutoff = Instant.now().plus(Duration.ofHours(1));
        ReferralCodeEntity referralCodeEntity = new ReferralCodeEntity(userId, code, cutoff);
        referralCodeRepository.save(referralCodeEntity);
        return code;
    }

    @Nullable
    public Long getReferralCode(UUID userId) {
        return referralCodeRepository.findById(userId).map(ReferralCodeEntity::code).orElse(null);
    }

    public void removeReferralCode(UUID userId) {
        referralCodeRepository.deleteById(userId);
    }

    @Scheduled(fixedDelay = TimeUtil.FIVE_MINUTES, initialDelay = TimeUtil.HALF_MINUTE)
    public void removeExpiredCodes() {
        removeExpiredReferralCodes();
    }
}
