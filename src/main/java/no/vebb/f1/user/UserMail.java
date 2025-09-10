package no.vebb.f1.user;

import no.vebb.f1.mail.domain.Email;

public record UserMail(UserEntity userEntity, Email email) {
}
