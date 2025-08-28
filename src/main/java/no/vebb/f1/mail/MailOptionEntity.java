package no.vebb.f1.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mail_options")
public class MailOptionEntity {
    @Id
    @Column(name = "mail_option")
    private int mailOption;

    public int mailOption() {
        return mailOption;
    }
}
