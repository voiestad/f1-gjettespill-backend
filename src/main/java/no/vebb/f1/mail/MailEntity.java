package no.vebb.f1.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "mailing_list")
public class MailEntity {
    @Id
    @Column(name = "user_id")
    private UUID id;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    protected MailEntity() {
    }

    public MailEntity(UUID id, String email) {
        this.id = id;
        this.email = email;
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }
}
