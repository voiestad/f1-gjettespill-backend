package no.vebb.f1.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class MailPreferenceId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "mail_option", nullable = false)
    private int mailOption;

    protected MailPreferenceId() {
    }

    public MailPreferenceId(UUID userId, int mailOption) {
        this.userId = userId;
        this.mailOption = mailOption;
    }

    public int mailOption() {
        return mailOption;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MailPreferenceId that)) return false;
        return mailOption == that.mailOption && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, mailOption);
    }
}
