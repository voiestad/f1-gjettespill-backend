package no.vebb.f1.mail.mailPreference;

import jakarta.persistence.*;
import no.vebb.f1.mail.mailOption.MailOption;


@Entity
@Table(name = "mail_preferences")
public class MailPreferenceEntity {
    @EmbeddedId
    private MailPreferenceId id;

    protected MailPreferenceEntity() {}

    public MailPreferenceEntity(MailPreferenceId id) {
        this.id = id;
    }

    public MailOption mailOption() {
        return id.mailOption();
    }
}
