package no.vebb.f1.mail;

import jakarta.persistence.*;


@Entity
@Table(name = "mail_preferences")
public class MailPreferenceEntity {
    @EmbeddedId
    private MailPreferenceId id;

    protected MailPreferenceEntity() {}

    public MailPreferenceEntity(MailPreferenceId id) {
        this.id = id;
    }

    public int mailOption() {
        return id.mailOption();
    }
}
