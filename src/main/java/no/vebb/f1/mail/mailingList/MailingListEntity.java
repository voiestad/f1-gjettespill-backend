package no.vebb.f1.mail.mailingList;

import jakarta.persistence.*;
import no.vebb.f1.mail.domain.Email;
import no.vebb.f1.user.UserEntity;

import java.util.UUID;

@Entity
@Table(name = "mailing_list")
public class MailingListEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Embedded
    private Email email;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    protected MailingListEntity() {
    }

    public MailingListEntity(UUID userId, Email email) {
        this.userId = userId;
        this.email = email;
    }

    public UUID userId() {
        return userId;
    }

    public Email email() {
        return email;
    }

    public UserEntity user() {
        return user;
    }
}
