package no.vebb.f1.mail;

import jakarta.persistence.*;


@Entity
@Table(name = "mail_preferences")
public class MailPreference {
    @EmbeddedId
    private MailPreferenceId id;

    protected MailPreference() {}

    public MailPreference(MailPreferenceId id) {
        this.id = id;
    }

    public int mailOption() {
        return id.mailOption();
    }
}
