package no.vebb.f1.mail;

import jakarta.persistence.*;
import no.vebb.f1.user.UserEntity;

import java.util.UUID;

@Entity
@Table(name = "mailing_list")
public class MailingListEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    protected MailingListEntity() {
    }

    public MailingListEntity(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public UUID userId() {
        return userId;
    }

    public String email() {
        return email;
    }

    public UserEntity user() {
        return user;
    }
}
