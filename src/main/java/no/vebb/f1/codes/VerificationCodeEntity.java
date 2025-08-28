package no.vebb.f1.codes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    @Column(nullable = false, name = "email")
    private String email;
    @Column(nullable = false, name = "cutoff")
    private Instant cutoff;

    protected VerificationCodeEntity() {
    }

    public VerificationCodeEntity(UUID userId, int code, String email, Instant cutoff) {
        this.userId = userId;
        this.code = code;
        this.email = email;
        this.cutoff = cutoff;
    }

    public int code() {
        return code;
    }

    public String email() {
        return email;
    }

    @Override
    public Instant cutoff() {
        return cutoff;
    }

}
