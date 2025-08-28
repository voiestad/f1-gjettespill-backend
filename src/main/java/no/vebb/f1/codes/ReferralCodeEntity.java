package no.vebb.f1.codes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "referral_codes")
public class ReferralCodeEntity implements Code {
    @Id
    @Column(name = "user_id")
    private UUID userId;
    @Column(nullable = false, name = "referral_code")
    private long code;
    @Column(nullable = false, name = "cutoff")
    private Instant cutoff;

    protected ReferralCodeEntity() {
    }

    public ReferralCodeEntity(UUID userId, long code, Instant cutoff) {
        this.userId = userId;
        this.code = code;
        this.cutoff = cutoff;
    }

    public long code() {
        return code;
    }

    @Override
    public Instant cutoff() {
        return cutoff;
    }

}
