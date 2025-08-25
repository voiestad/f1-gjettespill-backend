package no.vebb.f1.codes;

import no.vebb.f1.mail.MailService;
import no.vebb.f1.user.UserMail;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class CodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final ReferralCodeRepository referralCodeRepository;
    private final MailService mailService;

    public CodeService(VerificationCodeRepository verificationCodeRepository,
                       ReferralCodeRepository referralCodeRepository, MailService mailService) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.referralCodeRepository = referralCodeRepository;
        this.mailService = mailService;
    }

    public void sendVerificationCode(UserMail userMail) {
        final int code = CodeGenerator.getVerificationCode();
        VerificationCode verificationCode = new VerificationCode(
                userMail.user().id(),
                code,
                userMail.email(),
                Instant.now().plus(Duration.ofMinutes(10))
        );
        verificationCodeRepository.save(verificationCode);
        mailService.sendVerificationCodeMail(userMail, code);
    }

    public void removeVerificationCode(UUID userId) {
        verificationCodeRepository.deleteById(userId);
    }

    public void removeExpiredVerificationCodes() {
        verificationCodeRepository.deleteAll(
                verificationCodeRepository.findAll().stream().filter(this::isExpired).toList()
        );
    }

    private boolean isExpired(Code verificationCode) {
        return verificationCode.cutoff().compareTo(Instant.now()) < 0;
    }

    public boolean hasVerificationCode(UUID userId) {
        return verificationCodeRepository.findById(userId).isPresent();
    }

    public boolean validateVerificationCode(UUID userId, int code) {
        Optional<VerificationCode> optVerificationCode = verificationCodeRepository.findById(userId);
        if (optVerificationCode.isEmpty()) {
            return false;
        }
        VerificationCode verificationCode = optVerificationCode.get();
        boolean isValidCode = code == verificationCode.code();
        if (!isValidCode) {
            return false;
        }
        boolean isValidCutoff = verificationCode.cutoff().compareTo(Instant.now()) > 0;
        if (!isValidCutoff) {
            return false;
        }
        String email = verificationCode.email();
        mailService.addToMailingList(userId, email);
        removeVerificationCode(userId);
        return true;
    }

    public boolean isValidReferralCode(long code) {
        return referralCodeRepository.findByCode(code)
                .map(referralCode ->
                        referralCode.cutoff().compareTo(Instant.now()) > 0).orElse(false);
    }

    public void removeExpiredReferralCodes() {
        referralCodeRepository.deleteAll(
                referralCodeRepository.findAll().stream().filter(this::isExpired).toList()
        );

    }

    public long addReferralCode(UUID userId) {
        long code = CodeGenerator.getReferralCode();
        Instant cutoff = Instant.now().plus(Duration.ofHours(1));
        ReferralCode referralCode = new ReferralCode(userId, code, cutoff);
        referralCodeRepository.save(referralCode);
        return code;
    }

    @Nullable
    public Long getReferralCode(UUID userId) {
        return referralCodeRepository.findById(userId).map(ReferralCode::code).orElse(null);
    }

    public void removeReferralCode(UUID userId) {
        referralCodeRepository.deleteById(userId);
    }
}
