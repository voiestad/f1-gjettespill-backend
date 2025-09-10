package no.vebb.f1.mail.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.vebb.f1.exception.InvalidEmailException;

@Embeddable
public class Email {
    @Column(unique = true, nullable = false, name = "email")
    private String value;

    protected Email() {}

    public Email(String value) {
        this.value = value;
        validate();
    }

    private void validate() throws InvalidEmailException {
        if (!isValidEmail()) {
            throw new InvalidEmailException();
        }
    }

    private boolean isValidEmail() {
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
