package no.vebb.f1.mail;

import jakarta.persistence.*;

@Entity
@Table(name = "mail_options")
public class MailOptionEntity {
    @EmbeddedId
    private MailOption mailOption;

    public MailOption mailOption() {
        return mailOption;
    }
}
