package no.vebb.f1.codes;

import jakarta.persistence.*;
import no.vebb.f1.mail.domain.Email;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_codes")
public class VerificationCodeEntity implements Code {
    @Id
    @Column(name = "user_id")
    private UUID userId;
    @Column(nullable = false, name = "verification_code")
    private int code;
    @Embedded
    private Email email;
    @Column(nullable = false, name = "cutoff")
    private Instant cutoff;

    protected VerificationCodeEntity() {
    }

    public VerificationCodeEntity(UUID userId, int code, Email email, Instant cutoff) {
        this.userId = userId;
        this.code = code;
        this.email = email;
        this.cutoff = cutoff;
    }

    public int code() {
        return code;
    }

    public Email email() {
        return email;
    }

    @Override
    public Instant cutoff() {
        return cutoff;
    }

}
