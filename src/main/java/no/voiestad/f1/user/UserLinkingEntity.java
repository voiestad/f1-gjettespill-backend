package no.voiestad.f1.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_linkings")
public class UserLinkingEntity {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(nullable = false, name = "user_id")
    private UUID userId;
    @Column(nullable = false, name = "code")
    private String code;
    @Column(nullable = false, name = "valid_to")
    private Instant validTo;

    UserLinkingEntity(int id, UUID userId, String code, Instant validTo) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.validTo = validTo;
    }

    protected UserLinkingEntity() {}

    public UUID userId() {
        return userId;
    }

    public String code() {
        return code;
    }

    public Instant validTo() {
        return validTo;
    }

}
