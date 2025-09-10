package no.vebb.f1.user;

import no.vebb.f1.mail.Email;

public record UserMail(UserEntity userEntity, Email email) {
}
