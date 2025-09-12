package no.vebb.f1.mail.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Optional;

@Embeddable
public class Email {
    @Column(unique = true, nullable = false, name = "email")
    private String value;

    protected Email() {}

    private Email(String value) {
        this.value = value;
    }

    public static Optional<Email> getEmail(String value) {
        Email email = new Email(value);
        if (email.isValid()) {
            return Optional.of(email);
        }
        return Optional.empty();
    }

    private boolean isValid() {
        return value.matches("^([\\w\\-.])+(\\+?[\\w\\-.]+)?@([\\w\\-]+\\.)+[\\w\\-]{2,4}$");
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonValue
    public String toValue() {
        return value;
    }
}
